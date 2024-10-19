package at.technikum.swkom_ws2024_groupe.service;

import at.technikum.swkom_ws2024_groupe.customExceptions.DocumentNotFoundException;
import at.technikum.swkom_ws2024_groupe.customExceptions.InvalidFileUploadException;
import at.technikum.swkom_ws2024_groupe.entities.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentService {

    private Map<UUID, Document> documentStore = new HashMap<>();

    public Document uploadDocument(String name, String contentType, long size) {
        // Validate file type (maybe need for later?)
        /*if (!contentType.equals("application/pdf") && !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            throw new InvalidFileUploadException("Invalid file type. Only PDF and DOCX files are allowed.");
        }*/

        Document document = new Document(name, contentType, size);
        documentStore.put(document.getId(), document);
        return document;
    }

    public Optional<Document> getDocumentById(UUID id) {
        Document document = documentStore.get(id);
        if (document == null) {
            throw new InvalidFileUploadException("Document with ID " + id + " not found.");
        }
        return Optional.of(document);
    }

    public List<Document> getAllDocuments() {
        if (documentStore.isEmpty()) {
            throw new DocumentNotFoundException("No documents found.");
        }
        return new ArrayList<>(documentStore.values()); // Return all documents from the store
    }
}
