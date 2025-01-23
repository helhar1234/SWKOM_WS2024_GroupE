package at.technikum.paperlessrest.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    private String id;

    private String filename;
    private long filesize;
    private String filetype;
    private LocalDateTime uploadDate;
    private boolean ocrJobDone;

    public Document(String documentId, String filename) {
        this.id = documentId;
        this.filename = filename;
    }
}
