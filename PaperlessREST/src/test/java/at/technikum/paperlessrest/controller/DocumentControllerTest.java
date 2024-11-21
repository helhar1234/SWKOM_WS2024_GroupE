package at.technikum.paperlessrest.controller;

import at.technikum.paperlessrest.customExceptions.DocumentNotFoundException;
import at.technikum.paperlessrest.customExceptions.InvalidFileUploadException;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

class DocumentControllerTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final DocumentController documentController = new DocumentController(documentService);

    @Test
    void uploadFile_success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        Document document = new Document();
        document.setId("123e4567-e89b-12d3-a456-426614174000");

        when(documentService.uploadDocument(file)).thenReturn(document);

        // Act
        ResponseEntity<?> response = documentController.uploadFile(file);

        // Assert
        assertEquals(CREATED, response.getStatusCode());
        assertEquals(document.getId(), response.getBody());
        verify(documentService).uploadDocument(file);
    }

    @Test
    void uploadFile_invalidFileUpload() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "invalid.txt",
                "text/plain",
                "Sample text content".getBytes()
        );

        when(documentService.uploadDocument(invalidFile))
                .thenThrow(new InvalidFileUploadException("Invalid file type."));

        // Act
        ResponseEntity<?> response = documentController.uploadFile(invalidFile);

        // Assert
        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid file type.", response.getBody());
        verify(documentService).uploadDocument(invalidFile);
    }

    @Test
    void uploadFile_internalServerError() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        when(documentService.uploadDocument(file)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = documentController.uploadFile(file);

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("File upload failed.", response.getBody());
        verify(documentService).uploadDocument(file);
    }

    @Test
    void deleteDocument_success() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        when(documentService.deleteDocumentById(documentId)).thenReturn(true);

        // Act
        ResponseEntity<?> response = documentController.deleteDocument(documentId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentService).deleteDocumentById(documentId);
    }

    @Test
    void deleteDocument_failure() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        when(documentService.deleteDocumentById(documentId)).thenReturn(false);

        // Act
        ResponseEntity<?> response = documentController.deleteDocument(documentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Document not found", response.getBody());
        verify(documentService).deleteDocumentById(documentId);
    }

    @Test
    void deleteDocument_notFound() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        when(documentService.deleteDocumentById(documentId)).thenThrow(new DocumentNotFoundException("Document with ID " + documentId + " not found."));

        // Act
        ResponseEntity<?> response = documentController.deleteDocument(documentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Document with ID " + documentId + " not found.", response.getBody());
        verify(documentService).deleteDocumentById(documentId);
    }

    @Test
    void deleteDocument_internalServerError() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        when(documentService.deleteDocumentById(documentId)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = documentController.deleteDocument(documentId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error deleting document", response.getBody());
        verify(documentService).deleteDocumentById(documentId);
    }

    @Test
    void getDocument_success() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        Document document = new Document();
        document.setId(documentId.toString());
        document.setFilename("test.pdf");

        when(documentService.getDocumentById(documentId)).thenReturn(Optional.of(document));

        // Act
        ResponseEntity<?> response = documentController.getDocument(documentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(document, response.getBody());
        verify(documentService).getDocumentById(documentId);
    }

    @Test
    void getDocument_notFound() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        when(documentService.getDocumentById(documentId)).thenThrow(new DocumentNotFoundException("Document with ID " + documentId + " not found."));

        // Act
        ResponseEntity<?> response = documentController.getDocument(documentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Document with ID " + documentId + " not found.", response.getBody());
        verify(documentService).getDocumentById(documentId);
    }

    @Test
    void getDocument_internalServerError() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        when(documentService.getDocumentById(documentId)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = documentController.getDocument(documentId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while retrieving the document.", response.getBody());
        verify(documentService).getDocumentById(documentId);
    }

    @Test
    void getAllDocuments_success() {
        // Arrange
        Document document1 = new Document();
        document1.setId(UUID.randomUUID().toString());
        document1.setFilename("test1.pdf");

        Document document2 = new Document();
        document2.setId(UUID.randomUUID().toString());
        document2.setFilename("test2.pdf");

        List<Document> documents = Arrays.asList(document1, document2);

        when(documentService.getAllDocuments()).thenReturn(documents);

        // Act
        ResponseEntity<?> response = documentController.getAllDocuments();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(documents, response.getBody());
        verify(documentService).getAllDocuments();
    }

    @Test
    void getAllDocuments_internalServerError() {
        // Arrange
        when(documentService.getAllDocuments()).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = documentController.getAllDocuments();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while retrieving the document.", response.getBody());
        verify(documentService).getAllDocuments();
    }
}