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
    private String id; // UUID as the unique identifier for each document

    private String filename; // Original file name
    private long filesize; // File size in bytes
    private String filetype; // MIME type of the file (e.g., application/pdf)
    private LocalDateTime uploadDate; // Timestamp when the document was uploaded
}
