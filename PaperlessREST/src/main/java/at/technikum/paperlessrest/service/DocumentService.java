package at.technikum.paperlessrest.service;

import at.technikum.paperlessrest.dto.DocumentDTO;
import at.technikum.paperlessrest.dto.DocumentSearchResultDTO;
import at.technikum.paperlessrest.elastic.ElasticsearchSearcher;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.rabbitmq.RabbitMQSender;
import at.technikum.paperlessrest.repository.DocumentRepository;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentService {

    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;
    private final RabbitMQSender rabbitMQSender; // Producer for OCR-Jobs
    private final ElasticsearchSearcher elasticsearchSearcher;
    private final String bucketName = "documents";

    public DocumentService(MinioClient minioClient, DocumentRepository documentRepository, RabbitMQSender rabbitMQSender, ElasticsearchSearcher elasticsearchSearcher) {
        this.minioClient = minioClient;
        this.documentRepository = documentRepository;
        this.rabbitMQSender = rabbitMQSender;
        this.elasticsearchSearcher = elasticsearchSearcher;
    }

    public DocumentDTO uploadFile(MultipartFile file) throws Exception {
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            log.warn("Invalid file type for upload: {}", file.getContentType());
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        String id = UUID.randomUUID().toString();
        DocumentDTO document = DocumentDTO.builder()
                .id(id)
                .filename(file.getOriginalFilename())
                .filesize(file.getSize())
                .filetype(file.getContentType())
                .uploadDate(java.time.LocalDateTime.now())
                .ocrJobDone(false)
                .build();

        log.info("Saving document metadata to repository: {}", document);
        // Saving the entity in the repository
        documentRepository.save(new Document(document));

        // Cheching and building of the Bucket in MinIO
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.info("Bucket '{}' does not exist. Creating it now.", bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        log.info("Uploading file to MinIO: {}", document.getFilename());
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(id)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        log.info("File successfully uploaded to MinIO with ID: {}", id);
        rabbitMQSender.sendOCRJobMessage(id, document.getFilename());
        return document;
    }

    public void deleteDocument(String id) throws Exception {
        log.info("Request received to delete document with ID: {}", id);

        if (!documentRepository.existsById(id)) {
            log.warn("Document with ID {} not found", id);
            throw new IllegalArgumentException("Document not found");
        }

        log.info("Deleting document metadata from repository with ID: {}", id);
        documentRepository.deleteById(id);

        log.info("Removing file from MinIO with ID: {}", id);
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(id).build());
        log.info("Document with ID {} successfully deleted", id);
    }

    public byte[] getDocumentFileById(String id) throws Exception {
        log.info("Fetching file for document ID: {}", id);

        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(id)
                .build())) {
            byte[] fileData = stream.readAllBytes();
            log.info("File successfully retrieved for document ID: {}", id);
            return fileData;
        } catch (Exception e) {
            log.error("Error retrieving file for document ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public DocumentDTO getDocumentById(String id) {
        log.info("Fetching document metadata by ID: {}", id);
        return documentRepository.findById(id)
                .map(document -> new DocumentDTO(document.getId(), document.getFilename(), document.getFilesize(), document.getFiletype(), document.getUploadDate(), document.isOcrJobDone()))
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));
    }

    public List<DocumentDTO> getAllDocuments() {
        log.info("Fetching all documents.");
        return documentRepository.findAll()
                .stream()
                .filter(Objects::nonNull) // Remove `null`entries
                .map(document -> new DocumentDTO(
                        document.getId(),
                        document.getFilename(),
                        document.getFilesize(),
                        document.getFiletype(),
                        document.getUploadDate(),
                        document.isOcrJobDone()))
                .collect(Collectors.toList());
    }

    public List<DocumentDTO> searchDocuments(String query) {
        log.info("Querying Elasticsearch with query: {}", query);

        // Elasticsearch - actual search
        List<DocumentSearchResultDTO> elasticResults = elasticsearchSearcher.searchDocuments(query);

        log.info("Mapping Elasticsearch results to DTOs using database data.");

        // Retrieve document information from the database and map to DTOs
        return elasticResults.stream()
                .map(result -> {
                    try {
                        // Fetch the document from the database
                        Document document = documentRepository.findById(result.getDocumentId())
                                .orElseThrow(() -> new RuntimeException("Document not found in database for ID: " + result.getDocumentId()));

                        // Map the database data to a DocumentDTO
                        return new DocumentDTO(
                                document.getId(),
                                document.getFilename(),
                                document.getFilesize(),
                                document.getFiletype(),
                                document.getUploadDate(),
                                document.isOcrJobDone()
                        );
                    } catch (Exception e) {
                        log.error("Error fetching document data for ID {}: {}", result.getDocumentId(), e.getMessage(), e);
                        // If an error occurs, return an empty DTO or other handling
                        return new DocumentDTO(
                                result.getDocumentId(),
                                result.getFilename(),
                                result.getFilesize(),
                                result.getFiletype(),
                                result.getUploadDate(),
                                result.isOcrJobDone()
                        );
                    }
                })
                .collect(Collectors.toList());
    }

}
