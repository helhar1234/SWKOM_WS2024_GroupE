package at.technikum.worker.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticsearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public String indexDocument(String documentId, String ocrText) throws IOException {
        // Create the document content (JSON format)
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("documentId", documentId);
        jsonMap.put("ocrText", ocrText);
        jsonMap.put("@timestamp", Instant.now().toString()); // Add the @timestamp field

        // Create an index request
        IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                .index("documents")
                .id(documentId)
                .document(jsonMap)
        );

        // Send the index request to Elasticsearch
        IndexResponse response = elasticsearchClient.index(request);

        return response.id();
    }
}
