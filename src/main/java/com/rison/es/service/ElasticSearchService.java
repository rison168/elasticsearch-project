package com.rison.es.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParser;
import com.rison.es.main.Goods;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.elasticsearch.client.Request;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private RestClient restClient;

    /**
     * 创建索引
     */
    public void createIndex(List<Goods> list) throws IOException {
        BulkRequest request = new BulkRequest();
        list.forEach(x -> {
            request.add(new IndexRequest("jd_posts", "doc")
                    .source(XContentType.JSON,
                            "name", x.getName(),
                            "price", x.getPrice(),
                            "img", x.getImg()
                    ));
        });
        request.timeout(TimeValue.timeValueMinutes(2));
        request.timeout("2m");
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        request.setRefreshPolicy("wait_for");
        request.waitForActiveShards(2);
        request.waitForActiveShards(ActiveShardCount.ALL);
        BulkResponse bulkResponse = restHighLevelClient.bulk(request);

    }

    /**
     * 删除索引
     *
     * @param indexName
     * @throws IOException
     */
    public void deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));
        request.masterNodeTimeout("1m");
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        DeleteIndexResponse deleteIndexResponse = restHighLevelClient.indices().deleteIndex(request);
    }

    public List<Map<String, Object>> searchIndex(String indexName, String value, int current, int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //高亮，支持所有FileBean实体的字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        Goods fileBean = new Goods();
        String[] fieldNames = new String[fileBean.getClass().getDeclaredFields().length];
        int i = 0;
        for (Field f : fileBean.getClass().getDeclaredFields()) {
            HighlightBuilder.Field highlight = new HighlightBuilder.Field(f.getName());
            highlight.highlighterType("unified");
            highlightBuilder.field(highlight);
            fieldNames[i] = f.getName();
            i++;
        }
        //设置高亮样式
        highlightBuilder.preTags("<label style=\"color: red\">");
        highlightBuilder.postTags("</label>");
        //添加查询条件
        searchSourceBuilder.highlighter(highlightBuilder);
        //搜索也支持所有FileBean实体的字段
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(value, fieldNames));
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.from(current);
        searchSourceBuilder.size(size);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(JSON.toJSONString(searchResponse));
        //获取高亮字段
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits.getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            for (String fieldName : fieldNames) {
                HighlightField highlight = highlightFields.get(fieldName);
                System.out.println(fieldName);
                if (highlight != null) {
                    Text[] fragments = highlight.fragments();
                    String fragmentString = fragments[0].string();
                    System.out.println("高亮值：" + fragmentString);
                    Map<String, Object> map = hit.getSourceAsMap();
                    map.put(fieldName, fragmentString);
                    if (!result.contains(map)) {
                        result.add(map);
                    }
                }
            }
        }

        return result;
    }
}
