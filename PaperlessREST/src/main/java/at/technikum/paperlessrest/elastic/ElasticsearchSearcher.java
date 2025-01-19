package at.technikum.paperlessrest.elastic;

import at.technikum.paperlessrest.entities.DocumentSearchResult;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ElasticsearchSearcher {

    @Autowired
    ElasticsearchClient elasticsearchClient;

    public List<DocumentSearchResult> searchDocuments(String query) {
        try {
            // Elasticsearch-Suchanfrage erstellen
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("documents")
                    .query(q -> q
                            .match(m -> m
                                    .field("ocrText") // Nach OCR-Text suchen
                                    .query(query)
                            )
                    )
            );

            // Elasticsearch-Suche ausführen
            SearchResponse<DocumentSearchResult> searchResponse = elasticsearchClient.search(searchRequest, DocumentSearchResult.class);

            log.info("Elasticsearch returned {} results for query: {}", searchResponse.hits().hits().size(), query);

            // Ergebnisse mappen und null-Quellen filtern
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching documents in Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search documents", e);
        }
    }
}


