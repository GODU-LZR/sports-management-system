package com.example.venue.service.impl;

import com.alibaba.fastjson2.JSON; // 导入 Fastjson2
import com.example.venue.pojo.NearVenue;
import com.example.venue.pojo.NearVenueParam;
import com.example.venue.pojo.VenueDocument;
import com.example.venue.service.UserVenueServer;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoDistance; // <-- 导入 GeoDistance 枚举类
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserVenueServerImpl implements UserVenueServer {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    // Elasticsearch 索引名称 (请根据你的实际索引名称修改)
    private static final String VENUE_INDEX_NAME = "venue";

    // Elasticsearch 中存储地理位置的字段名称 (请根据你的实际字段名称修改，该字段类型应为 geo_point)
    private static final String LOCATION_FIELD_NAME = "location";

    // Elasticsearch 中用于全文搜索的字段名称 (请根据你的实际字段名称修改)
    private static final String[] SEARCHABLE_FIELDS = {"name", "position"}; // 例如：在 name 字段中搜索关键字

    // 默认返回数量，如果 NearVenueParam.limit 未指定或小于等于0
    private static final int DEFAULT_LIMIT = 10;

    @Override
    public List<VenueDocument> searchNearVenue(NearVenueParam param) {
        // 1. 创建搜索请求
        SearchRequest searchRequest = new SearchRequest(VENUE_INDEX_NAME);

        // 2. 创建搜索源构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 3. 构建 Bool Query，结合关键字搜索和地理位置过滤 (如果需要)
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 3.1 添加关键字搜索条件 (MUST)
        if (param.getKey() != null && !param.getKey().trim().isEmpty()) {
            // 使用 multi_match 在多个字段中搜索关键字
            // SEARCHABLE_FIELDS 需要定义为类常量，例如 {"name", "position", "sport"}
            boolQuery.must(QueryBuilders.multiMatchQuery(param.getKey(), SEARCHABLE_FIELDS));
        } else {
            // 如果没有关键字，则匹配所有文档
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        // 4. 解析地理位置并添加按距离排序
        GeoPoint centerPoint = null; // 用于存储解析后的中心点
        if (param.getLocation() != null && !param.getLocation().trim().isEmpty()) {
            try {
                // 解析地理位置字符串 "latitude, longitude"
                String[] latLon = param.getLocation().split(",");
                if (latLon.length == 2) {
                    double latitude = Double.parseDouble(latLon[0].trim());
                    double longitude = Double.parseDouble(latLon[1].trim());
                    centerPoint = new GeoPoint(latitude, longitude);

                    // 添加按距离排序 (PRIMARY SORT)
                    // 按照从中心点到文档位置的距离升序排序
                    // LOCATION_FIELD_NAME 需要定义为类常量，例如 "location"
                    sourceBuilder.sort(SortBuilders.geoDistanceSort(LOCATION_FIELD_NAME, centerPoint)
                            .order(SortOrder.ASC) // 升序，最近的在前
                            .unit(DistanceUnit.KILOMETERS) // 排序距离单位，这里固定为公里，你可以根据需要修改
                            .geoDistance(GeoDistance.ARC)); // 使用 GeoDistance.ARC 枚举

                    // TODO: 如果需要限定搜索半径，可以在这里添加 geo_distance filter 到 boolQuery 中
                    // if (param.getDistance() != null && param.getDistance() > 0) {
                    //     // param.getDistanceUnit() 需要在 NearVenueParam 中定义
                    //     DistanceUnit unit = DistanceUnit.fromString(param.getDistanceUnit());
                    //     boolQuery.filter(QueryBuilders.geoDistanceQuery(LOCATION_FIELD_NAME)
                    //             .point(centerPoint)
                    //             .distance(param.getDistance(), unit));
                    // }

                } else {
                    System.err.println("Invalid location format: " + param.getLocation() + ". Expected 'latitude, longitude'. Ignoring location filter and sort.");
                    // 根据需求处理无效的位置格式
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid latitude or longitude format in location: " + param.getLocation() + ". Error: " + e.getMessage() + ". Ignoring location filter and sort.");
                // 处理解析数字错误
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid distance unit. Error: " + e.getMessage() + ". Ignoring distance filter.");
                // 处理无效的距离单位，这里只影响可选的filter
            }
        }
        // 如果没有提供有效的 location，则只进行关键字搜索和默认排序 (通常是按相关性 _score)
        // 如果提供了 location 但解析失败，也会回退到只进行关键字搜索和默认排序。


        // 将构建好的查询设置到 sourceBuilder
        sourceBuilder.query(boolQuery);

        // 5. 设置返回数量 (Limit)
        int limit = (param.getLimit() != null && param.getLimit() > 0) ? param.getLimit() : DEFAULT_LIMIT;
        sourceBuilder.size(limit); // 设置返回数量
        sourceBuilder.from(0); // 不进行分页，始终从第一个结果开始

        // 将 sourceBuilder 设置到 searchRequest
        searchRequest.source(sourceBuilder);

        // **修改返回类型和列表**
        List<VenueDocument> venueList = new ArrayList<>();

        // 6. 执行搜索
        try {
            System.out.println("Executing Elasticsearch search for nearby venues...");
            SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println("Search completed. Hits: " + searchResponse.getHits().getTotalHits().value);


            // 7. 处理搜索结果
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                if (sourceAsString != null && !sourceAsString.isEmpty()) {
                    try {
                        // **直接反序列化为 VenueDocument**
                        VenueDocument venue = JSON.parseObject(sourceAsString, VenueDocument.class);

                        // **移除获取和设置 calculatedDistance 的代码**
                        // 因为 VenueDocument 没有这个字段

                        venueList.add(venue);
                    } catch (Exception jsonParseError) {
                        System.err.println("Error parsing JSON source to VenueDocument for hit ID " + hit.getId() + ": " + jsonParseError.getMessage());
                        jsonParseError.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            // 捕获 Elasticsearch 客户端操作中的 IO 异常
            System.err.println("Error searching for nearby venues in Elasticsearch: " + e.getMessage());
            e.printStackTrace();
            // 根据你的业务需求，可以选择抛出自定义异常或返回空列表
            // throw new RuntimeException("Failed to search nearby venues", e);
            return new ArrayList<>(); // 发生异常时返回空列表
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during Elasticsearch search: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // 捕获其他可能的异常
        }
        return venueList; // 返回 VenueDocument 列表
    }
}
