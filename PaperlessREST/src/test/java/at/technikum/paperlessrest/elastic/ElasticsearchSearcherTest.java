package at.technikum.paperlessrest.elastic;

import at.technikum.paperlessrest.dto.DocumentSearchResultDTO;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElasticsearchSearcherTest {

    private final ElasticsearchClient elasticsearchClient = mock(ElasticsearchClient.class);
    private final ElasticsearchSearcher elasticsearchSearcher = new ElasticsearchSearcher();

    @BeforeEach
    void setup() {
        elasticsearchSearcher.elasticsearchClient = elasticsearchClient;
    }

    @Test
    void searchDocuments_success() throws Exception {
        // Arrange
        String query = "test";

        DocumentSearchResultDTO documentSearchResult = new DocumentSearchResultDTO("1", "text", "filename", "filetype", 1, true, null, "timestamp");
        Hit<DocumentSearchResultDTO> hit = Hit.of(h -> h
                .id("id")
                .index("documents")
                .source(documentSearchResult)
        );

        HitsMetadata<DocumentSearchResultDTO> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));

        SearchResponse<DocumentSearchResultDTO> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class))).thenReturn(searchResponse);

        // Act
        List<DocumentSearchResultDTO> results = elasticsearchSearcher.searchDocuments(query);

        // Assert
        assertEquals(1, results.size());
        assertEquals("1", results.get(0).getDocumentId());
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class));
    }

    @Test
    void searchDocuments_noResults() throws Exception {
        // Arrange
        String query = "empty";

        HitsMetadata<DocumentSearchResultDTO> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Collections.emptyList());

        SearchResponse<DocumentSearchResultDTO> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class))).thenReturn(searchResponse);

        // Act
        List<DocumentSearchResultDTO> results = elasticsearchSearcher.searchDocuments(query);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class));
    }

    @Test
    void searchDocuments_elasticError() throws Exception {
        // Arrange
        String query = "error";

        when(elasticsearchClient.search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class)))
                .thenThrow(new RuntimeException("Elasticsearch failure"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> elasticsearchSearcher.searchDocuments(query));
        assertEquals("Failed to search documents", exception.getMessage());
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class));
    }

    @Test
    void searchDocuments_nullSourceInResult() throws Exception {
        // Arrange
        String query = "nullSource";

        Hit<DocumentSearchResultDTO> hit = Hit.of(h -> h
                .id("id")
                .index("documents")
                .source(null) // Quelle ist null
        );

        HitsMetadata<DocumentSearchResultDTO> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));

        SearchResponse<DocumentSearchResultDTO> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class))).thenReturn(searchResponse);

        // Act
        List<DocumentSearchResultDTO> results = elasticsearchSearcher.searchDocuments(query);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(DocumentSearchResultDTO.class));
    }
}
