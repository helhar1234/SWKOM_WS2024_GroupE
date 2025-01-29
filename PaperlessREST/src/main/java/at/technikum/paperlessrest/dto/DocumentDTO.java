package at.technikum.paperlessrest.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class DocumentDTO {
    private String id;

    private String filename;
    private long filesize;
    private String filetype;
    private LocalDateTime uploadDate;
    private boolean ocrJobDone;
}

