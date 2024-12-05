package at.technikum.paperlessrest.service;

import at.technikum.paperlessrest.customExceptions.DocumentNotFoundException;
import at.technikum.paperlessrest.customExceptions.InvalidFileUploadException;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final DocumentService documentService = new DocumentService(documentRepository);

    @Test
    void uploadDocument_success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        Document savedDocument = new Document();
        savedDocument.setId(UUID.randomUUID().toString());
        savedDocument.setFilename(file.getOriginalFilename());
        savedDocument.setFilesize(file.getSize());
        savedDocument.setFiletype(file.getContentType());
        savedDocument.setFile(file.getBytes());
        savedDocument.setUploadDate(LocalDateTime.now());

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        // Act
        Document result = documentService.uploadDocument(file);

        // Assert
        assertNotNull(result);
        assertEquals(file.getOriginalFilename(), result.getFilename());
        assertEquals(file.getSize(), result.getFilesize());
        assertEquals(file.getContentType(), result.getFiletype());
        assertArrayEquals(file.getBytes(), result.getFile());

        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void uploadDocument_fileIsEmpty() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // Act & Assert
        InvalidFileUploadException exception = assertThrows(InvalidFileUploadException.class,
                () -> documentService.uploadDocument(emptyFile)
        );

        assertEquals("File is empty. Please upload a valid file.", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void uploadDocument_invalidFileType() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Sample text content".getBytes()
        );

        // Act & Assert
        InvalidFileUploadException exception = assertThrows(InvalidFileUploadException.class,
                () -> documentService.uploadDocument(invalidFile)
        );

        assertEquals("Invalid file type. Only PDF files are allowed.", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void uploadDocument_failedToReadFile() throws IOException {
        // Arrange
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenThrow(new IOException("Failed to read file"));

        // Act & Assert
        InvalidFileUploadException exception = assertThrows(InvalidFileUploadException.class,
                () -> documentService.uploadDocument(file)
        );

        assertEquals("Failed to read file content.", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void deleteDocumentById_shouldDeleteSuccessfully_whenDocumentExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        String idString = id.toString();
        when(documentRepository.existsById(idString)).thenReturn(true);

        // Act
        boolean result = documentService.deleteDocumentById(id);

        // Assert
        assertTrue(result);
        verify(documentRepository, times(1)).existsById(idString);
        verify(documentRepository, times(1)).deleteById(idString);
    }

    @Test
    void deleteDocumentById_shouldThrowException_whenDocumentDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        String idString = id.toString();
        when(documentRepository.existsById(idString)).thenReturn(false);

        // Act & Assert
        DocumentNotFoundException exception = assertThrows(DocumentNotFoundException.class, () -> {
            documentService.deleteDocumentById(id);
        });

        assertEquals("Document with ID " + id + " not found.", exception.getMessage());
        verify(documentRepository, times(1)).existsById(idString);
        verify(documentRepository, never()).deleteById(idString);
    }

    @Test
    void getDocumentById_success() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();

        Document document = new Document();
        document.setId(documentIdString);
        document.setFilename("test.pdf");

        when(documentRepository.findById(documentIdString)).thenReturn(Optional.of(document));

        // Act
        Optional<Document> result = documentService.getDocumentById(documentId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(documentIdString, result.get().getId());
        assertEquals("test.pdf", result.get().getFilename());

        verify(documentRepository).findById(documentIdString);
    }

    @Test
    void getDocumentById_documentNotFound() {
        // Arrange
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();

        when(documentRepository.findById(documentIdString)).thenReturn(Optional.empty());

        // Act & Assert
        DocumentNotFoundException exception = assertThrows(DocumentNotFoundException.class,
                () -> documentService.getDocumentById(documentId)
        );

        assertEquals("Document with ID " + documentId + " not found.", exception.getMessage());
        verify(documentRepository).findById(documentIdString);
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

        when(documentRepository.findAll()).thenReturn(documents);

        // Act
        List<Document> result = documentService.getAllDocuments();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test1.pdf", result.get(0).getFilename());
        assertEquals("test2.pdf", result.get(1).getFilename());

        verify(documentRepository).findAll();
    }

    @Test
    void getAllDocuments_noDocuments() {
        // Arrange
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Document> result = documentService.getAllDocuments();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(documentRepository).findAll();
    }
}