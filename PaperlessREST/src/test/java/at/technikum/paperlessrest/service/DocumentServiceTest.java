package at.technikum.paperlessrest.service;

import at.technikum.paperlessrest.dto.DocumentDTO;
import at.technikum.paperlessrest.dto.DocumentSearchResultDTO;
import at.technikum.paperlessrest.dto.DocumentWithFileDTO;
import at.technikum.paperlessrest.elastic.ElasticsearchSearcher;
import at.technikum.paperlessrest.entities.Document;
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

        DocumentDTO document = DocumentDTO.builder()
                .id(UUID.randomUUID().toString())
                .filename(file.getOriginalFilename())
                .filesize(file.getSize())
                .filetype(file.getContentType())
                .uploadDate(LocalDateTime.now())
                .ocrJobDone(false)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(new Document(document));
        doNothing().when(rabbitMQSender).sendOCRJobMessage(anyString(), anyString());
        doAnswer(invocation -> null).when(minioClient).putObject(any(PutObjectArgs.class));

        // Act
        DocumentDTO result = documentService.uploadFile(file);

        // Assert
        assertNotNull(result);
        assertEquals(file.getOriginalFilename(), result.getFilename());
        verify(documentRepository).save(any(Document.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(rabbitMQSender).sendOCRJobMessage(result.getId(), result.getFilename());
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
        verify(rabbitMQSender, never()).sendOCRJobMessage(anyString(), anyString());
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
        DocumentDTO document1 = DocumentDTO.builder().id("1").filename("doc1.pdf").build();
        DocumentDTO document2 = DocumentDTO.builder().id("2").filename("doc2.pdf").build();

        when(documentRepository.findAll()).thenReturn(List.of(new Document(document1), new Document(document2)));

        // Act
        List<DocumentDTO> result = documentService.getAllDocuments();

        // Assert
        assertEquals(2, result.size());
        verify(documentRepository).findAll();
        assertEquals(document1.getId(), result.get(0).getId());
        assertEquals(document2.getId(), result.get(1).getId());
    }

    @Test
    void getAllDocuments_noDocumentsFound() throws Exception {
        // Arrange
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<DocumentDTO> result = documentService.getAllDocuments();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentRepository).findAll();
        verify(minioClient, never()).getObject(any(GetObjectArgs.class));
    }

    /*
    @Test
    void searchDocuments_successWithResultsFromElasticsearch() {
        // Arrange
        String query = "test";

        DocumentSearchResultDTO elasticResult = new DocumentSearchResultDTO("1", "text", "filename", "filetype", 1, true, null, "timestamp");
        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(List.of(elasticResult));

        // Act
        List<DocumentWithFileDTO> results = documentService.searchDocuments(query);

        // Assert
        assertEquals(1, results.size());
        assertEquals(elasticResult.getDocumentId(), results.get(0).getId());
        verify(elasticsearchSearcher).searchDocuments(query);
    }

    @Test
    void searchDocuments_noResultsFromElasticsearch() {
        // Arrange
        String query = "unknown";

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(Collections.emptyList());

        // Act
        List<DocumentWithFileDTO> results = documentService.searchDocuments(query);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(elasticsearchSearcher).searchDocuments(query);
    }

    @Test
    void searchDocuments_elasticSearchErrorHandling() {
        // Arrange
        String query = "error";

        when(elasticsearchSearcher.searchDocuments(query)).thenThrow(new RuntimeException("Elasticsearch error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.searchDocuments(query));
        assertEquals("Elasticsearch error", exception.getMessage());
        verify(elasticsearchSearcher).searchDocuments(query);
    }

    @Test
    void searchDocuments_minioFileRetrievalFailure() throws Exception {
        // Arrange
        String query = "minioFail";
        DocumentSearchResultDTO elasticResult = new DocumentSearchResultDTO("1", "text", "filename", "filetype", 1, true, null, "timestamp");

        when(elasticsearchSearcher.searchDocuments(query)).thenReturn(List.of(elasticResult));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("MinIO error"));

        // Act
        List<DocumentWithFileDTO> results = documentService.searchDocuments(query);

        // Assert
        assertEquals(1, results.size());
        assertNull(results.get(0).getFile());
        verify(elasticsearchSearcher).searchDocuments(query);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }
    */

}
