package at.technikum.paperlessrest.entities;

public class DocumentWithFile {
    private Document document;
    private byte[] file;

    public DocumentWithFile(Document document, byte[] file) {
        this.document = document;
        this.file = file;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}

