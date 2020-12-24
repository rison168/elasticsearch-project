package com.rison.es.controller;

import com.rison.es.service.ElasticSearchService;
import com.rison.es.utils.HTMLParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    /**
     * 创建索引
     * @throws IOException
     */
    @GetMapping("/create/index")
    private String createIndex() throws IOException {
        elasticSearchService.createIndex(HTMLParse.getGoodsList());
        return "插入成功!";
    }

    /**
     * 删除索引
     * @throws IOException
     */
    @GetMapping("/delete/index")
    private String deleteIndex(String indexName) throws IOException {
        elasticSearchService.deleteIndex(indexName);
        return "删除成功!";
    }
    /**
     * 搜索索引
     * @throws IOException
     */
    @GetMapping("/search/index")
    private List<Map<String, Object>> searchIndex(String indexName, String value) throws IOException {
        return elasticSearchService.searchIndex(indexName, value, 1, 30);
    }
    @GetMapping
    public String sayHello() {
        return "jd";
    }
}
