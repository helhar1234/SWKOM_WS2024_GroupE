package at.technikum.paperlessrest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class DocumentWithFileDTO {
    private String id;

    private String filename;
    private long filesize;
    private String filetype;
    private LocalDateTime uploadDate;
    private boolean ocrJobDone;
    private byte[] file;

    public DocumentWithFileDTO(String id, String filename, long filesize, String filetype, LocalDateTime uploadDate, boolean ocrJobDone, byte[] file) {
        this.id = id;
        this.filename = filename;
        this.filesize = filesize;
        this.filetype = filetype;
        this.uploadDate = uploadDate;
        this.ocrJobDone = ocrJobDone;
        this.file = file;
    }

    public DocumentWithFileDTO(DocumentDTO documentDTO, byte[] file) {
        this.id = documentDTO.getId();
        this.filename = documentDTO.getFilename();
        this.filesize = documentDTO.getFilesize();
        this.filetype = documentDTO.getFiletype();
        this.uploadDate = documentDTO.getUploadDate();
        this.ocrJobDone = documentDTO.isOcrJobDone();
        this.file = file;
    }
}
