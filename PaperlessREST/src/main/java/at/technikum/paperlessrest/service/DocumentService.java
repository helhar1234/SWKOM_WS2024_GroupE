package at.technikum.paperlessrest.service;

import at.technikum.paperlessrest.customExceptions.DocumentNotFoundException;
import at.technikum.paperlessrest.customExceptions.InvalidFileUploadException;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document uploadDocument(MultipartFile file) {
        // Validate the file
        if (file == null || file.isEmpty()) {
            throw new InvalidFileUploadException("File is empty. Please upload a valid file.");
        }

        String filename = file.getOriginalFilename();
        String filetype = file.getContentType();

        // Validate MIME type and file extension
        if (filetype == null || !filetype.equals("application/pdf") ||
                (filename != null && !filename.toLowerCase().endsWith(".pdf"))) {
            throw new InvalidFileUploadException("Invalid file type. Only PDF files are allowed.");
        }

        // Build the Document object
        Document document = new Document();
        try {
            document.setId(UUID.randomUUID().toString());
            document.setFilename(filename);
            document.setFilesize(file.getSize());
            document.setFiletype(filetype);
            document.setFile(file.getBytes());
            document.setUploadDate(LocalDateTime.now());
        } catch (IOException e) {
            throw new InvalidFileUploadException("Failed to read file content.");
        }

        // Save the document to the database and return it
        return documentRepository.save(document);
    }

    public boolean deleteDocumentById(UUID id) {
        String idString = id.toString();
        if (!documentRepository.existsById(idString)) {
            throw new DocumentNotFoundException("Document with ID " + id + " not found.");
        }
        documentRepository.deleteById(idString);
        return true;
    }

    public Optional<Document> getDocumentById(UUID id) {
        String idString = id.toString();
        return documentRepository.findById(idString)
                .or(() -> {
                    throw new DocumentNotFoundException("Document with ID " + id + " not found.");
                });
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
}
