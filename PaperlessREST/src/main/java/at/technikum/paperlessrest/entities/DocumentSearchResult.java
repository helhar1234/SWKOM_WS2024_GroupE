package at.technikum.paperlessrest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResult {
    private String documentId;
    private String ocrText;

    @JsonProperty("@timestamp") // Mappt das Elasticsearch-Feld "@timestamp" auf "timestamp"
    private String timestamp;
}
