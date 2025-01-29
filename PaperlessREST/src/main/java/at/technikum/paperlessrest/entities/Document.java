package at.technikum.paperlessrest.entities;

import at.technikum.paperlessrest.dto.DocumentDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
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
    private byte[] file;

    public Document(String documentId, String filename) {
        this.id = documentId;
        this.filename = filename;
    }

    public Document(DocumentDTO documentDTO) {
        this.id = documentDTO.getId();
        this.filename = documentDTO.getFilename();
        this.filesize = documentDTO.getFilesize();
        this.filetype = documentDTO.getFiletype();
        this.uploadDate = documentDTO.getUploadDate();
        this.ocrJobDone = documentDTO.isOcrJobDone();
    }
}
