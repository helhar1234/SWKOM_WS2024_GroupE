package at.technikum.paperlessrest.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name ="files")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Document {
    @Id
    private String id;
    private String filename;
    private long filesize;
    private String filetype;
    private byte[] file;
    private LocalDateTime uploadDate;

}
