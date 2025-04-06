package com.olive.base.util;

import com.olive.base.utils.LocalDateUtil;
import org.apache.http.HttpHost;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jhlz
 * @version x.x.x
 */
public class EsTest {

    /**
     * example:
     */
    @Test
    public void search_es_test() throws IOException {

        SearchRequest searchRequest = new SearchRequest("ik_test");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建查询条件
        sourceBuilder.query(QueryBuilders.multiMatchQuery("组织", "title", "content"));

        // 设置分页
        // sourceBuilder.from(from);
        // sourceBuilder.size(size);

        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").preTags("<em>").postTags("</em>");
        highlightBuilder.field("content").preTags("<em>").postTags("</em>");
        sourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(sourceBuilder);
        SearchResponse response = getClient().search(searchRequest, RequestOptions.DEFAULT);


        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            // 处理高亮结果
            Map<String, String> highlightFields = hit.getHighlightFields().values().stream()
                    .collect(Collectors.toMap(
                            hf -> hf.getName(),
                            hf -> Arrays.stream(hf.getFragments()).map(f -> f.string()).collect(Collectors.joining("..."))
                    ));

            sourceAsMap.put("highlight", highlightFields);
            results.add(sourceAsMap);
        }

        System.out.println(results);

    }

    /**
     * example: 批量插入
     */
    @Test
    public void bulk_doc_test() {
        IndexRequest request = new IndexRequest("ik_test");

        Map<String, String> map = Map.of("title", "第三个", "content", """
                产生共同的感情，自然形成一种行为准则或习惯，要求个人服从。这就构成了“非正式组织”，这种非正式组织对于工人的行为影响很大，是影响生产效率的重要因素。"""
        );
        request.source(map);

        IndexRequest request1 = new IndexRequest("ik_test");
        request1.source(
                Map.of("title", "测试记录2", "content", "组织架构我的你的不存在的",
                        "link", "http://localhost/hahhah",
                        "createTime", LocalDateUtil.dateTime()
                )
        );

        BulkRequest bulkRequest = new BulkRequest().add(request, request1);

        try {
            BulkResponse response = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(Arrays.toString(response.getItems()));
            System.out.println(response.status().getStatus());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * example:
     */
    @Test
    public void create_index_test() {
        try {

            CreateIndexRequest createIndexRequest = new CreateIndexRequest("ik_test");

            XContentBuilder mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("title")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_max_word")
                    .endObject()
                    .startObject("content")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_max_word")
                    .endObject()
                    .endObject()
                    .endObject();
            createIndexRequest.mapping(mapping);
            createIndexRequest.settings(Settings.builder()
                    .put("index.number_of_shards", 1)
                    .put("index.number_of_replicas", 0)
            );
            boolean exists = getClient().indices().exists(new GetIndexRequest("ik_test"), RequestOptions.DEFAULT);
            System.out.println(exists);
            //
            // CreateIndexResponse response = getClient().indices().create(createIndexRequest, RequestOptions.DEFAULT);
            // System.out.println("创建结果：" + response.isAcknowledged());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * example: es
     */
    @Test
    public void es_test() throws IOException {
        RestClientBuilder build = RestClient
                .builder(HttpHost.create("http://localhost:9200"));
        try (RestHighLevelClient client = new RestHighLevelClient(build);) {
            // 获取所有索引
            GetIndexResponse response = client.indices().get(new GetIndexRequest("*"), RequestOptions.DEFAULT);
            System.out.println(Arrays.toString(response.getIndices()));
            SearchResponse resume = client.search(
                    new SearchRequest("resume")
                            .source(
                                    new SearchSourceBuilder()
                                            .highlighter(
                                                    new HighlightBuilder().field("filename")
                                            )
                                            .query(
                                                    new MatchQueryBuilder("filename", "newname")
                                            )
                            ),
                    RequestOptions.DEFAULT
            );
            System.out.println(Arrays.toString(resume.getHits().getHits()));
        }
    }

    /**
     * example: pdf parser
     */
    @Test
    public void pdf_content_parser_test() throws IOException {
        File file = new File("D:\\download\\11.pdf");
        try (PDDocument document = Loader.loadPDF(
                new RandomAccessReadBufferedFile(file)
        );) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            System.out.println(text);
        }
    }

    static RestHighLevelClient getClient() {
        return new RestHighLevelClient(
                RestClient.builder(HttpHost.create("http://localhost:9200"))
        );
    }
}
