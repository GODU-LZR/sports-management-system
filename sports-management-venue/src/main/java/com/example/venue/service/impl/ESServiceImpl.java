package com.example.venue.service.impl;

import com.example.venue.dto.VenueAddRequest;
import com.example.venue.dto.VenueSearchRequest;
import com.example.venue.pojo.es.ESVenue;
import com.example.venue.service.ESService;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ESServiceImpl implements ESService {

    private final RestHighLevelClient elasticsearchClient;

    private static final String VENUE_INDEX_NAME = "venue"; // 索引名使用复数形式

    @Autowired // 使用构造器注入 RestHighLevelClient
    public ESServiceImpl(RestHighLevelClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public boolean createTable() {
        try {
            // 1. 构建映射并转为JSON字符串
            String mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("venue_id").field("type", "keyword").endObject()
                    .startObject("sport_id").field("type", "keyword").endObject()
                    .startObject("name")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_smart")
                    .endObject()
                    .startObject("position")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_smart")
                    .endObject()
                    .startObject("value").field("type", "double").endObject()
                    .startObject("location").field("type", "geo_point").endObject()
                    .startObject("state").field("type", "integer").endObject()
                    .endObject()
                    .endObject()
                    .toString(); // 关键修改：转换为String

            // 2. 创建索引请求
            CreateIndexRequest request = new CreateIndexRequest(VENUE_INDEX_NAME)
                    .mapping(mapping); // 现在传入String类型

            // 3. 执行创建
            CreateIndexResponse response = elasticsearchClient.indices()
                    .create(request, RequestOptions.DEFAULT);

            return response.isAcknowledged();

        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean addVenue(VenueAddRequest venueAddRequest) {
        try {
            // 1. 构建文档源数据（包含geo_point转换）
            XContentBuilder sourceBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("venue_id", venueAddRequest.getVenueId())
                    .field("sport_id", venueAddRequest.getSportId())
                    .field("name", venueAddRequest.getName())
                    .field("position", venueAddRequest.getPosition())
                    .field("value", venueAddRequest.getValue())
                    .startObject("location")  // geo_point特殊处理
                    .field("latitude", venueAddRequest.getLatitude())
                    .field("longitude", venueAddRequest.getLongitude())
                    .endObject()
                    .field("state", venueAddRequest.getState())
                    .field("created_id", venueAddRequest.getCreatedId())
                    .field("updated_id", venueAddRequest.getUpdatedId())
                    .endObject();

            // 2. 创建索引请求
            IndexRequest request = new IndexRequest(VENUE_INDEX_NAME)
                    .id(venueAddRequest.getVenueId()) // 使用venue_id作为文档ID
                    .source(sourceBuilder);

            // 3. 执行插入
            IndexResponse response = elasticsearchClient.index(request, RequestOptions.DEFAULT);

            return response.status() == RestStatus.CREATED ||
                    response.status() == RestStatus.OK;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<ESVenue> searchVenue(VenueSearchRequest request) {
        try {
            // 1. 构建搜索请求
            SearchRequest searchRequest = new SearchRequest(VENUE_INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 2. 构建布尔查询（组合所有条件）
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("sport_id", request.getSportId()))
                    .must(QueryBuilders.rangeQuery("value")
                            .gte(request.getValue())
                            .lte(request.getValue() + 20))
                    .filter(QueryBuilders.geoDistanceQuery("location")  // 注意字段名与Venue类一致
                            .point(request.getLatitude(), request.getLongitude())
                            .distance(request.getDistance(), DistanceUnit.METERS));

            sourceBuilder.query(boolQuery);

            // 3. 设置返回字段（适配GeoPoint结构）
            sourceBuilder.fetchSource(new String[]{
                    "venue_id",  // 对应Venue类的venueId字段
                    "name",     // 对应Venue类的name字段
                    "location"  // 对应Venue类的GeoPoint类型字段
            }, null);

            // 4. 执行查询
            searchRequest.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            // 5. 解析结果（处理GeoPoint的特殊结构）
            return Arrays.stream(response.getHits().getHits())
                    .map(hit -> {
                        Map<String, Object> source = hit.getSourceAsMap();

                        // 解析GeoPoint（ES存储为{"lat":xx, "lon":yy}或[lon,lat]数组）
                        Map<String, Object> locationMap = (Map<String, Object>) source.get("location");
                        double lat, lon;

                        if (locationMap.containsKey("lat")) {  // 对象形式
                            lat = ((Number) locationMap.get("lat")).doubleValue();
                            lon = ((Number) locationMap.get("lon")).doubleValue();
                        } else {  // 数组形式（ES可能存储为[lon, lat]）
                            List<Double> coords = (List<Double>) locationMap;
                            lon = coords.get(0);
                            lat = coords.get(1);
                        }

                        return ESVenue.builder()
                                .venueId((String) source.get("venue_id"))
                                .name((String) source.get("name"))
                                .location(new GeoPoint(lat, lon))  // 直接构造GeoPoint对象
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public boolean deleteVenue(String venueId) {
        try {
            // 1. 构建删除请求
            DeleteRequest request = new DeleteRequest(VENUE_INDEX_NAME)
                    .id(venueId); // 直接使用 venueId 作为文档 _id
            // 2. 执行删除
            DeleteResponse response = elasticsearchClient.delete(request, RequestOptions.DEFAULT);

            // 3. 判断结果
            if (response.status() == RestStatus.OK || response.status() == RestStatus.NOT_FOUND) {
                return response.getResult() == DeleteResponse.Result.DELETED; // 仅当实际删除时返回 true
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
