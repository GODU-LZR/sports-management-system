package com.example.venue.service.impl; // 根据你的项目结构修改包名


import com.alibaba.fastjson2.JSON; // 导入 Fastjson2
import com.example.venue.pojo.VenueDocument;
import com.example.venue.service.ElasticsearchServer; // 导入 Service 接口
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.*; // <-- 确保导入正确
import org.elasticsearch.common.xcontent.*;// <-- 确保导入正确
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 服务实现类。
 * 负责与 Elasticsearch 集群进行低级别交互，如索引管理和文档操作。
 */
@Service // 标记这是一个 Spring Service
public class ElasticsearchServerImpl implements ElasticsearchServer {

    private final RestHighLevelClient elasticsearchClient;

    // 索引名称常量
    private static final String VENUE_INDEX_NAME = "venue"; // 使用指定的索引名称 "venue"

    @Autowired // 使用构造器注入 RestHighLevelClient
    public ElasticsearchServerImpl(RestHighLevelClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * 创建 Elasticsearch 中的场馆索引 (venue)。
     * 定义了 venueId, sport, name, value, location 字段的映射。
     *
     * @return 如果索引创建成功或已存在，返回 true；如果创建过程中发生错误，返回 false。
     */
    @Override
    public boolean createVenueIndex() {
        try {
            // 1. 检查索引是否已存在
            GetIndexRequest getIndexRequest = new GetIndexRequest(VENUE_INDEX_NAME);
            boolean indexExists = elasticsearchClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

            if (indexExists) {
                System.out.println("Elasticsearch index '" + VENUE_INDEX_NAME + "' already exists.");
                return true; // 索引已存在，认为成功
            }

            // 2. 如果索引不存在，创建索引请求
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(VENUE_INDEX_NAME);

            // 3. 定义索引的映射 (Mapping)
            XContentBuilder mappingBuilder = XContentFactory.jsonBuilder();
            mappingBuilder.startObject();
            mappingBuilder.startObject("properties"); // 定义字段属性

            // venueId: 唯一编号，字符串类型 -> 使用 keyword
            mappingBuilder.startObject("venueId").field("type", "keyword").endObject();

            // sport: 场地类型，字符串类型 -> 使用 keyword (适合过滤和聚合)
            mappingBuilder.startObject("sport").field("type", "keyword").endObject();

            // name: 场地名字，字符串类型 -> 使用 text (适合全文搜索)，并添加 keyword 子字段 (适合精确匹配和排序)
            mappingBuilder.startObject("name")
                    .field("type", "text")
                    .startObject("fields")
                    .startObject("keyword")
                    .field("type", "keyword")
                    .field("ignore_above", 256) // 忽略超过256长度的字符串，避免内存问题
                    .endObject()
                    .endObject()
                    .endObject();

            // position: 新增字段，假设是描述性位置信息，使用 text/keyword
            mappingBuilder.startObject("position")
                    .field("type", "text")
                    .startObject("fields")
                    .startObject("keyword")
                    .field("type", "keyword")
                    .field("ignore_above", 256)
                    .endObject()
                    .endObject()
                    .endObject();

            // value: 价格，浮点类型 -> 使用 float
            mappingBuilder.startObject("value").field("type", "float").endObject();

            // state: 新增字段，场地状态，整数类型 -> 使用 integer
            mappingBuilder.startObject("state").field("type", "integer").endObject();

            // location: 位置经纬度，GEO地理位置类型 -> 使用 geo_point
            mappingBuilder.startObject("location").field("type", "geo_point").endObject();


            mappingBuilder.endObject(); // End properties
            mappingBuilder.endObject(); // End mappings

            // 4. 将构建好的映射设置到创建索引请求中
            createIndexRequest.mapping(mappingBuilder);

            // 5. 执行创建索引请求
            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);

            // 6. 检查响应状态
            boolean acknowledged = createIndexResponse.isAcknowledged(); // 索引创建请求是否被集群接受
            boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged(); // 主分片是否已分配

            if (acknowledged && shardsAcknowledged) {
                System.out.println("Elasticsearch index '" + VENUE_INDEX_NAME + "' created successfully.");
                return true;
            } else {
                System.err.println("Elasticsearch index '" + VENUE_INDEX_NAME + "' creation acknowledged but shards not acknowledged.");
                // 这通常表示集群有问题，分片未能正常分配
                return false;
            }

        } catch (org.elasticsearch.ElasticsearchStatusException e) {
            // 捕获特定异常，如索引已存在 (虽然上面已经检查，但这是双重保险)
            if (e.status() == RestStatus.BAD_REQUEST && e.getMessage().contains("resource_already_exists_exception")) {
                System.out.println("Elasticsearch index '" + VENUE_INDEX_NAME + "' already exists (caught exception).");
                return true; // 索引已存在，认为成功
            } else {
                System.err.println("Elasticsearch status exception during index creation: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            // 捕获 IO 异常 (网络问题等)
            System.err.println("IO Exception during Elasticsearch index creation: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            // 捕获其他可能的异常
            System.err.println("An unexpected error occurred during Elasticsearch index creation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean indexVenue(VenueDocument venueDocument) {
        if (venueDocument == null || venueDocument.getVenueId() == null || venueDocument.getVenueId().trim().isEmpty()) {
            System.err.println("Cannot index venue: venueDocument or venueId is null or empty.");
            return false;
        }

        // 创建 IndexRequest
        IndexRequest request = new IndexRequest(VENUE_INDEX_NAME);

        // 设置文档 ID，使用 VenueDocument 的 venueId
        request.id(venueDocument.getVenueId());

        // 将 VenueDocument 对象转换为 JSON 字符串作为文档源
        String jsonSource = JSON.toJSONString(venueDocument);

        // 设置请求源为 JSON 字符串
        request.source(jsonSource, XContentType.JSON);

        try {
            // 执行索引请求
            IndexResponse indexResponse = elasticsearchClient.index(request, RequestOptions.DEFAULT);

            // 检查响应状态
            RestStatus status = indexResponse.status();
            if (status == RestStatus.CREATED || status == RestStatus.OK) {
                // CREATED 表示新文档被创建
                // OK 表示现有文档被更新 (如果使用了相同的 ID)
                System.out.println("Document indexed successfully. Index: " + indexResponse.getIndex()
                        + ", ID: " + indexResponse.getId()
                        + ", Version: " + indexResponse.getVersion()
                        + ", Status: " + status);
                return true;
            } else {
                System.err.println("Failed to index document. Index: " + indexResponse.getIndex()
                        + ", ID: " + indexResponse.getId()
                        + ", Status: " + status);
                return false;
            }

        } catch (IOException e) {
            System.err.println("IO Exception during Elasticsearch indexing: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during Elasticsearch indexing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public List<VenueDocument> findAllVenues() {
        List<VenueDocument> venueList = new ArrayList<>();
        try {
            // 1. 创建 SearchRequest
            SearchRequest searchRequest = new SearchRequest(VENUE_INDEX_NAME);

            // 2. 创建 SearchSourceBuilder
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 3. 设置查询条件为 match_all (匹配所有文档)
            sourceBuilder.query(QueryBuilders.matchAllQuery());

            // 4. 设置返回的最大文档数量
            // 注意：默认只返回10条，这里设置为 MAX_SEARCH_SIZE
            sourceBuilder.size(10);

            // 5. 将 SearchSourceBuilder 添加到 SearchRequest
            searchRequest.source(sourceBuilder);

            // 6. 执行搜索请求
            System.out.println("Executing Elasticsearch search for all venues...");
            SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println("Search completed. Hits: " + searchResponse.getHits().getTotalHits().value);

            // 7. 处理搜索结果
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits.getHits()) {
                // 获取文档源 (JSON 字符串)
                String sourceAsString = hit.getSourceAsString();
                if (sourceAsString != null && !sourceAsString.isEmpty()) {
                    // 将 JSON 字符串转换为 VenueDocument 对象
                    try {
                        VenueDocument venue = JSON.parseObject(sourceAsString, VenueDocument.class);
                        venueList.add(venue);
                    } catch (Exception jsonParseError) {
                        System.err.println("Error parsing JSON source to VenueDocument for hit ID " + hit.getId() + ": " + jsonParseError.getMessage());
                        jsonParseError.printStackTrace();
                        // 可以选择跳过当前文档或记录错误
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("IO Exception during Elasticsearch search: " + e.getMessage());
            e.printStackTrace();
            // 返回空列表表示失败
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during Elasticsearch search: " + e.getMessage());
            e.printStackTrace();
            // 返回空列表表示失败
            return new ArrayList<>();
        }

        return venueList; // 返回找到的文档列表
    }

    @Override
    public boolean deleteVenueIndex() {
        try {
            // 1. 创建 DeleteIndexRequest
            DeleteIndexRequest request = new DeleteIndexRequest(VENUE_INDEX_NAME);

            // 2. 执行删除请求
            System.out.println("Attempting to delete Elasticsearch index '" + VENUE_INDEX_NAME + "'...");
            AcknowledgedResponse deleteIndexResponse = elasticsearchClient.indices().delete(request, RequestOptions.DEFAULT);

            // 3. 检查响应是否被确认 (acknowledged)
            boolean acknowledged = deleteIndexResponse.isAcknowledged();

            if (acknowledged) {
                System.out.println("Elasticsearch index '" + VENUE_INDEX_NAME + "' deleted successfully.");
                return true;
            } else {
                System.err.println("Elasticsearch index '" + VENUE_INDEX_NAME + "' deletion not acknowledged.");
                return false;
            }

        } catch (org.elasticsearch.ElasticsearchStatusException e) {
            // 捕获索引不存在的异常，并视为成功删除
            if (e.status() == RestStatus.NOT_FOUND) {
                System.out.println("Elasticsearch index '" + VENUE_INDEX_NAME + "' not found. No deletion needed.");
                return true;
            } else {
                System.err.println("Elasticsearch status exception during index deletion: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            System.err.println("IO Exception during Elasticsearch index deletion: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during Elasticsearch index deletion: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // 你可以在这里添加其他实现的方法，例如索引文档等
}
