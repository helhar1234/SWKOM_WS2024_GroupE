package at.technikum.paperlessrest.service;

import at.technikum.paperlessrest.elastic.ElasticsearchSearcher;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.entities.DocumentSearchResult;
import at.technikum.paperlessrest.entities.DocumentWithFile;
import at.technikum.paperlessrest.rabbitmq.RabbitMQSender;
import at.technikum.paperlessrest.repository.DocumentRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.ErrorResponse;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    private final MinioClient minioClient = mock(MinioClient.class);
    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final RabbitMQSender rabbitMQSender = mock(RabbitMQSender.class);
    private final ElasticsearchSearcher elasticsearchSearcher = mock(ElasticsearchSearcher.class);
    private final DocumentService documentService = new DocumentService(minioClient, documentRepository, rabbitMQSender, elasticsearchSearcher);

    @Test
    void uploadFile_success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        Document document = Document.builder()
                .id(UUID.randomUUID().toString())
                .filename(file.getOriginalFilename())
                .filesize(file.getSize())
                .filetype(file.getContentType())
                .uploadDate(LocalDateTime.now())
                .ocrJobDone(false)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(document);
        doNothing().when(rabbitMQSender).sendOCRJobMessage(anyString());
        // Statt `doNothing` hier `doAnswer` verwenden
        doAnswer(invocation -> null).when(minioClient).putObject(any(PutObjectArgs.class));

        // Act
        Document result = documentService.uploadFile(file);

        // Assert
        assertNotNull(result);
        assertEquals(file.getOriginalFilename(), result.getFilename());
        verify(documentRepository).save(any(Document.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(rabbitMQSender).sendOCRJobMessage(result.getId());
    }

    @Test
    void uploadFile_invalidFileType() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Sample text content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> documentService.uploadFile(invalidFile));

        assertEquals("Only PDF files are allowed.", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
        verify(rabbitMQSender, never()).sendOCRJobMessage(anyString());
    }

    @Test
    void uploadFile_bucketCreationFails() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doThrow(new RuntimeException("Bucket creation failed"))
                .when(minioClient).makeBucket(any(MakeBucketArgs.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.uploadFile(file));
        assertEquals("Bucket creation failed", exception.getMessage());
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void deleteDocument_success() throws Exception {
        // Arrange
        String documentId = UUID.randomUUID().toString();

        when(documentRepository.existsById(documentId)).thenReturn(true);
        doNothing().when(documentRepository).deleteById(documentId);
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act
        documentService.deleteDocument(documentId);

        // Assert
        verify(documentRepository).deleteById(documentId);
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteDocument_notFound() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Arrange
        String documentId = UUID.randomUUID().toString();

        when(documentRepository.existsById(documentId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> documentService.deleteDocument(documentId));

        assertEquals("Document not found", exception.getMessage());
        verify(documentRepository, never()).deleteById(documentId);
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteDocument_minioDeletionFails() throws Exception {
        // Arrange
        String documentId = UUID.randomUUID().toString();
        when(documentRepository.existsById(documentId)).thenReturn(true);
        doThrow(new RuntimeException("Deletion failed"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.deleteDocument(documentId));
        assertEquals("Deletion failed", exception.getMessage());
        verify(documentRepository).existsById(documentId);
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void getDocumentFileById_success() throws Exception {
        // Arrange
        String documentId = UUID.randomUUID().toString();
        byte[] fileContent = "Sample PDF content".getBytes();
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

        when(getObjectResponse.readAllBytes()).thenReturn(fileContent);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        // Act
        byte[] result = documentService.getDocumentFileById(documentId);

        // Assert
        assertArrayEquals(fileContent, result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getDocumentFileById_notFound() throws Exception {
        // Arrange
        String documentId = UUID.randomUUID().toString();
        ErrorResponse errorResponse = new ErrorResponse("NoSuchKey error", "Error message", null, null, null, null, null);
        Response response = mock(Response.class);

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new ErrorResponseException(errorResponse, response, "NoSuchKey error"));

        // Act & Assert
        Exception exception = assertThrows(ErrorResponseException.class, () -> documentService.getDocumentFileById(documentId));

        assertNotNull(exception.getMessage());
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getDocumentFileById_minioFails() throws Exception {
        // Arrange
        String documentId = UUID.randomUUID().toString();
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("MinIO error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.getDocumentFileById(documentId));
        assertEquals("MinIO error", exception.getMessage());
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getAllDocuments_success() throws Exception {
        // Arrange
        Document document1 = Document.builder().id("1").filename("doc1.pdf").build();
        Document document2 = Document.builder().id("2").filename("doc2.pdf").build();

        byte[] fileContent1 = "Content 1".getBytes();
        byte[] fileContent2 = "Content 2".getBytes();

        GetObjectResponse response1 = mock(GetObjectResponse.class);
        GetObjectResponse response2 = mock(GetObjectResponse.class);

        when(response1.readAllBytes()).thenReturn(fileContent1);
        when(response2.readAllBytes()).thenReturn(fileContent2);

        when(documentRepository.findAll()).thenReturn(List.of(document1, document2));

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        // Act
        List<DocumentWithFile> result = documentService.getAllDocuments();

        // Assert
        assertEquals(2, result.size());
        assertArrayEquals(fileContent1, result.get(0).getFile());
        assertArrayEquals(fileContent2, result.get(1).getFile());
        verify(documentRepository).findAll();
        verify(minioClient, times(2)).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getAllDocuments_noDocumentsFound() throws Exception {
        // Arrange
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<DocumentWithFile> result = documentService.getAllDocuments();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentRepository).findAll();
        verify(minioClient, never()).getObject(any(GetObjectArgs.class));
    }

    @Test
    void searchDocuments_successWithResultsFromBoth() {
        // Arrange
        String query = "test";

        Document document1 = Document.builder().id("1").filename("test1.pdf").build();
        Document document2 = Document.builder().id("2").filename("test2.pdf").build();

        DocumentSearchResult elasticResult = new DocumentSearchResult("1", "Text", "timestamp");

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(List.of(elasticResult));
        when(documentRepository.findById("1")).thenReturn(Optional.of(document1));
        when(documentRepository.findByFilenameContainingIgnoreCase(query)).thenReturn(List.of(document2));

        // Act
        List<DocumentWithFile> results = documentService.searchDocuments(query);

        // Assert
        assertEquals(2, results.size());
        assertEquals(document1.getId(), results.get(0).getDocument().getId());
        assertEquals(document2.getId(), results.get(1).getDocument().getId());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(documentRepository).findById("1");
        verify(documentRepository).findByFilenameContainingIgnoreCase(query);
    }

    @Test
    void searchDocuments_successWithOnlyElasticResults() {
        // Arrange
        String query = "elasticOnly";

        Document document1 = Document.builder().id("1").filename("elastic1.pdf").build();

        DocumentSearchResult elasticResult = new DocumentSearchResult("1", "Text", "timestamp");

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(List.of(elasticResult));
        when(documentRepository.findById("1")).thenReturn(Optional.of(document1));
        when(documentRepository.findByFilenameContainingIgnoreCase(query)).thenReturn(Collections.emptyList());

        // Act
        List<DocumentWithFile> results = documentService.searchDocuments(query);

        // Assert
        assertEquals(1, results.size());
        assertEquals(document1.getId(), results.get(0).getDocument().getId());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(documentRepository).findById("1");
        verify(documentRepository).findByFilenameContainingIgnoreCase(query);
    }

    @Test
    void searchDocuments_successWithOnlyDatabaseResults() {
        // Arrange
        String query = "dbOnly";

        Document document2 = Document.builder().id("2").filename("db2.pdf").build();

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(Collections.emptyList());
        when(documentRepository.findByFilenameContainingIgnoreCase(query)).thenReturn(List.of(document2));

        // Act
        List<DocumentWithFile> results = documentService.searchDocuments(query);

        // Assert
        assertEquals(1, results.size());
        assertEquals(document2.getId(), results.get(0).getDocument().getId());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(documentRepository).findByFilenameContainingIgnoreCase(query);
    }

    @Test
    void searchDocuments_noResults() {
        // Arrange
        String query = "nonexistent";

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(Collections.emptyList());
        when(documentRepository.findByFilenameContainingIgnoreCase(query)).thenReturn(Collections.emptyList());

        // Act
        List<DocumentWithFile> results = documentService.searchDocuments(query);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(documentRepository).findByFilenameContainingIgnoreCase(query);
    }

    @Test
    void searchDocuments_elasticAndDatabaseDuplicates() {
        // Arrange
        String query = "duplicate";

        Document document1 = Document.builder().id("1").filename("duplicate.pdf").build();

        DocumentSearchResult elasticResult = new DocumentSearchResult("1", "Text", "timestamp");

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(List.of(elasticResult));
        when(documentRepository.findById("1")).thenReturn(Optional.of(document1));
        when(documentRepository.findByFilenameContainingIgnoreCase(query)).thenReturn(List.of(document1));

        // Act
        List<DocumentWithFile> results = documentService.searchDocuments(query);

        // Assert
        assertEquals(1, results.size());
        assertEquals(document1.getId(), results.get(0).getDocument().getId());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(documentRepository).findById("1");
        verify(documentRepository).findByFilenameContainingIgnoreCase(query);
    }

    @Test
    void searchDocuments_elasticQueryFails() {
        // Arrange
        String query = "error";
        when(elasticsearchSearcher.searchDocuments(query)).thenThrow(new RuntimeException("Elasticsearch error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.searchDocuments(query));
        assertEquals("Elasticsearch error", exception.getMessage());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(documentRepository, never()).findByFilenameContainingIgnoreCase(anyString());
    }

    @Test
    void searchDocuments_databaseQueryFails() {
        // Arrange
        String query = "dbError";
        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(Collections.emptyList());
        when(documentRepository.findByFilenameContainingIgnoreCase(query)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.searchDocuments(query));
        assertEquals("Database error", exception.getMessage());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(documentRepository).findByFilenameContainingIgnoreCase(query);
    }

    @Test
    void searchDocuments_minioFailsForFile() throws Exception {
        // Arrange
        String query = "minioFail";

        Document document1 = Document.builder().id("1").filename("doc1.pdf").build();

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(Collections.emptyList());
        when(documentRepository.findByFilenameContainingIgnoreCase(query)).thenReturn(List.of(document1));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("MinIO error"));

        // Act
        List<DocumentWithFile> results = documentService.searchDocuments(query);

        // Assert
        assertEquals(1, results.size());
        assertNull(results.get(0).getFile());
        verify(documentRepository).findByFilenameContainingIgnoreCase(query);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }
}
