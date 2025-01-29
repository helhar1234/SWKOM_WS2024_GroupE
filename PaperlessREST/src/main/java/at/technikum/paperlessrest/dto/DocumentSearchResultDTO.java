package at.technikum.paperlessrest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResultDTO {
    private String documentId;
    private String ocrText;
    private String filename;
    private String filetype;
    private long filesize;
    private boolean ocrJobDone;
    private LocalDateTime uploadDate;

    @JsonProperty("@timestamp") // Mappt das Elasticsearch-Feld "@timestamp" auf "timestamp"
    private String timestamp;
}
