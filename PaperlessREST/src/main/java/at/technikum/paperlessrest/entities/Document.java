package at.technikum.paperlessrest.entities;

import java.util.UUID;

public class Document {
    private UUID id;
    private String name;
    private String contentType;
    private long size;

    // Constructor
    public Document(String name, String contentType, long size) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.contentType = contentType;
        this.size = size;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }
}
