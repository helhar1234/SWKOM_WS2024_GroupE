package at.technikum.paperlessrest.service;

import at.technikum.paperlessrest.elastic.ElasticsearchSearcher;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.entities.DocumentSearchResult;
import at.technikum.paperlessrest.entities.DocumentWithFile;
import at.technikum.paperlessrest.rabbitmq.RabbitMQSender;
import at.technikum.paperlessrest.repository.DocumentRepository;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentService {

    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;
    private final RabbitMQSender rabbitMQSender; // Ersetzen RabbitTemplate durch RabbitMQProducer
    private final ElasticsearchSearcher elasticsearchSearcher;
    private final String bucketName = "documents";

    public DocumentService(MinioClient minioClient, DocumentRepository documentRepository, RabbitMQSender rabbitMQSender, ElasticsearchSearcher elasticsearchSearcher) {
        this.minioClient = minioClient;
        this.documentRepository = documentRepository;
        this.rabbitMQSender = rabbitMQSender;
        this.elasticsearchSearcher = elasticsearchSearcher;
    }

    public Document uploadDocument(MultipartFile file) throws Exception {
        //log.info("Received file for upload: {}",file.getOriginalFilename());
        if (!file.getContentType().equals("application/pdf")) {
            log.warn("Invalid file type for upload: {}", file.getContentType());
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        String id = UUID.randomUUID().toString();
        Document document = Document.builder()
                .id(id)
                .filename(file.getOriginalFilename())
                .filesize(file.getSize())
                .filetype(file.getContentType())
                .uploadDate(java.time.LocalDateTime.now())
                .ocrJobDone(false)
                .build();

        //log.info("Saving document metadata to database: {}", document);
        documentRepository.save(document);

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.warn("Bucket '{}' does not exist. Creating it now.", bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        System.out.println("Uploading file to MinIO: {}" + document.getFilename());
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(id)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        log.info("File successfully uploaded to MinIO with ID: {}" + id);
        rabbitMQSender.sendOCRJobMessage(id);
        return document;
    }

    public void deleteDocument(String id) throws Exception {
        log.info("Request received to delete document with ID: {}", id);

        if (!documentRepository.existsById(id)) {
            log.warn("Document with ID {} not found", id);
            throw new IllegalArgumentException("Document not found");
        }

        log.info("Deleting document metadata from database with ID: {}", id);
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

    public List<DocumentWithFile> getAllDocuments() {
        log.info("Fetching all documents with associated files.");
        return documentRepository.findAll().stream().map(document -> {
            try {
                byte[] file = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(document.getId()).build()).readAllBytes();
                log.info("File successfully fetched for document ID: {}", document.getId());
                return new DocumentWithFile(document, file);
            } catch (Exception e) {
                log.error("Error fetching file for document ID {}: {}", document.getId(), e.getMessage(), e);
                return new DocumentWithFile(document, null);
            }
        }).collect(Collectors.toList());
    }
    public Document getDocumentById(String id) {
        //log.info("Fetching document metadata by ID: {}", id);
        return documentRepository.findById(id).orElse(null);
    }

    public List<DocumentWithFile> searchDocuments(String query) {
        // Elasticsearch-Suche
        log.info("Querying Elasticsearch with query: {}", query);
        List<DocumentSearchResult> elasticResults = elasticsearchSearcher.searchDocuments(query);

        // Datenbank-Suche nach Dateinamen und IDs
        log.info("Querying database for filenames and IDs containing: {}", query);
        List<Document> databaseMatches = new ArrayList<>();
        databaseMatches.addAll(documentRepository.findByFilenameContainingIgnoreCase(query));
        databaseMatches.addAll(documentRepository.findByIdContainingIgnoreCase(query));

        // Ergebnisse zusammenführen
        log.info("Merging Elasticsearch and database results");
        List<Document> allDocuments = new ArrayList<>();

        // Elasticsearch-Ergebnisse zur Liste hinzufügen
        for (DocumentSearchResult result : elasticResults) {
            Document document = documentRepository.findById(result.getDocumentId()).orElse(null);
            if (document != null) {
                allDocuments.add(document);
            }
        }

        // Datenbank-Ergebnisse zur Liste hinzufügen (ohne Duplikate)
        allDocuments.addAll(
                databaseMatches.stream()
                        .filter(doc -> allDocuments.stream().noneMatch(d -> d.getId().equals(doc.getId())))
                        .collect(Collectors.toList())
        );

        // Zu DocumentWithFile mappen
        log.info("Fetching file data for {} documents", allDocuments.size());
        return allDocuments.stream().map(document -> {
            try {
                byte[] file = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(document.getId())
                        .build()).readAllBytes();
                return new DocumentWithFile(document, file);
            } catch (Exception e) {
                log.error("Error fetching file for document ID {}: {}", document.getId(), e.getMessage());
                return new DocumentWithFile(document, null);
            }
        }).collect(Collectors.toList());
    }


}
