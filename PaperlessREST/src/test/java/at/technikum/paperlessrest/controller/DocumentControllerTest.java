package at.technikum.paperlessrest.controller;

import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.entities.DocumentWithFile;
import at.technikum.paperlessrest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

class DocumentControllerTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final DocumentController documentController = new DocumentController(documentService, null);

    @Test
    void uploadFile_success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        Document document = new Document();
        document.setId("123e4567-e89b-12d3-a456-426614174000");

        when(documentService.uploadFile(file)).thenReturn(document);

        // Act
        ResponseEntity<Document> response = documentController.uploadFile(file);

        // Assert
        assertEquals(CREATED, response.getStatusCode());
        assertEquals(document, response.getBody());
        verify(documentService).uploadFile(file);
    }

    @Test
    void uploadFile_invalidFileFormat() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid.txt",
                "text/plain",
                "Sample text content".getBytes()
        );

        when(documentService.uploadFile(file)).thenThrow(new IllegalArgumentException("Invalid file format."));

        // Act
        ResponseEntity<Document> response = documentController.uploadFile(file);

        // Assert
        assertEquals(BAD_REQUEST, response.getStatusCode());
        verify(documentService).uploadFile(file);
    }

    @Test
    void uploadFile_internalServerError() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        when(documentService.uploadFile(file)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<Document> response = documentController.uploadFile(file);

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(documentService).uploadFile(file);
    }

    @Test
    void uploadFile_emptyFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        when(documentService.uploadFile(file)).thenThrow(new IllegalArgumentException("File is empty."));

        // Act
        ResponseEntity<Document> response = documentController.uploadFile(file);

        // Assert
        assertEquals(BAD_REQUEST, response.getStatusCode());
        verify(documentService).uploadFile(file);
    }

    @Test
    void deleteDocument_success() throws Exception {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        doNothing().when(documentService).deleteDocument(documentId);

        // Act
        ResponseEntity<Void> response = documentController.deleteDocument(documentId);

        // Assert
        assertEquals(NO_CONTENT, response.getStatusCode());
        verify(documentService).deleteDocument(documentId);
    }

    @Test
    void deleteDocument_notFound() throws Exception {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        doThrow(new IllegalArgumentException("Document not found"))
                .when(documentService).deleteDocument(documentId);

        // Act
        ResponseEntity<Void> response = documentController.deleteDocument(documentId);

        // Assert
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(documentService).deleteDocument(documentId);
    }

    @Test
    void deleteDocument_internalServerError() throws Exception {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        doThrow(new RuntimeException("Unexpected error")).when(documentService).deleteDocument(documentId);

        // Act
        ResponseEntity<Void> response = documentController.deleteDocument(documentId);

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(documentService).deleteDocument(documentId);
    }

    @Test
    void getDocumentById_success() throws Exception {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        byte[] fileContent = "Sample PDF content".getBytes();
        Document document = new Document();
        document.setId(documentId);
        document.setFilename("test.pdf");

        when(documentService.getDocumentById(documentId)).thenReturn(document);
        when(documentService.getDocumentFileById(documentId)).thenReturn(fileContent);

        // Act
        ResponseEntity<byte[]> response = documentController.getDocumentById(documentId);

        // Assert
        assertEquals(OK, response.getStatusCode());
        assertArrayEquals(fileContent, response.getBody());
        verify(documentService).getDocumentById(documentId);
        verify(documentService).getDocumentFileById(documentId);
    }

    @Test
    void getDocumentById_notFound() {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";

        when(documentService.getDocumentById(documentId)).thenReturn(null);

        // Act
        ResponseEntity<byte[]> response = documentController.getDocumentById(documentId);

        // Assert
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(documentService).getDocumentById(documentId);
    }

    @Test
    void getDocumentById_internalServerError() {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        when(documentService.getDocumentById(documentId)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<byte[]> response = documentController.getDocumentById(documentId);

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(documentService).getDocumentById(documentId);
    }

    @Test
    void getAllDocuments_success() {
        // Arrange
        Document document1 = new Document();
        document1.setId("1");
        document1.setFilename("doc1.pdf");

        byte[] fileContent1 = "Content 1".getBytes();

        Document document2 = new Document();
        document2.setId("2");
        document2.setFilename("doc2.pdf");

        byte[] fileContent2 = "Content 2".getBytes();

        DocumentWithFile doc1 = new DocumentWithFile(document1, fileContent1);
        DocumentWithFile doc2 = new DocumentWithFile(document2, fileContent2);

        List<DocumentWithFile> documents = List.of(doc1, doc2);

        when(documentService.getAllDocuments()).thenReturn(documents);

        // Act
        ResponseEntity<List<DocumentWithFile>> response = documentController.getAllDocuments();

        // Assert
        assertEquals(OK, response.getStatusCode());
        assertEquals(documents, response.getBody());
        verify(documentService).getAllDocuments();
    }

    @Test
    void getAllDocuments_internalServerError() {
        // Arrange
        when(documentService.getAllDocuments()).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<List<DocumentWithFile>> response = documentController.getAllDocuments();

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(documentService).getAllDocuments();
    }

    @Test
    void searchDocuments_success() {
        // Arrange
        String query = "test";
        Document document = new Document();
        document.setId("1");
        document.setFilename("test.pdf");

        byte[] fileContent = "Sample PDF content".getBytes();

        DocumentWithFile doc = new DocumentWithFile(document, fileContent);

        List<DocumentWithFile> results = List.of(doc);

        when(documentService.searchDocuments(query)).thenReturn(results);

        // Act
        ResponseEntity<List<DocumentWithFile>> response = documentController.searchDocuments(query);

        // Assert
        assertEquals(OK, response.getStatusCode());
        assertEquals(results, response.getBody());
        verify(documentService).searchDocuments(query);
    }

    @Test
    void searchDocuments_invalidQuery() {
        // Act
        ResponseEntity<List<DocumentWithFile>> response = documentController.searchDocuments("");

        // Assert
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void searchDocuments_internalServerError() {
        // Arrange
        String query = "test";
        when(documentService.searchDocuments(query)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<List<DocumentWithFile>> response = documentController.searchDocuments(query);

        // Assert
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(documentService).searchDocuments(query);
    }
}