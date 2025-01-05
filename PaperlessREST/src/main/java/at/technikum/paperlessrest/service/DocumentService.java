package at.technikum.paperlessrest.service;

import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.entities.DocumentWithFile;
import at.technikum.paperlessrest.repository.DocumentRepository;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentService {

    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String bucketName = "documents";

    public DocumentService(MinioClient minioClient, DocumentRepository documentRepository, RabbitTemplate rabbitTemplate) {
        this.minioClient = minioClient;
        this.documentRepository = documentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Document uploadDocument(MultipartFile file) throws Exception {
        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        String id = UUID.randomUUID().toString();
        Document document = Document.builder()
                .id(id)
                .filename(file.getOriginalFilename())
                .filesize(file.getSize())
                .filetype(file.getContentType())
                .uploadDate(java.time.LocalDateTime.now())
                .build();

        documentRepository.save(document);

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(id)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        sendOCRJobMessage(id);
        return document;
    }

    public void deleteDocument(String id) throws Exception {
        if (!documentRepository.existsById(id)) {
            throw new IllegalArgumentException("Document not found");
        }

        documentRepository.deleteById(id);
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(id).build());
    }

    public byte[] getDocumentFileById(String id) throws Exception {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(id)
                .build())) {
            return stream.readAllBytes();
        }
    }

    public List<DocumentWithFile> getAllDocuments() {
        return documentRepository.findAll().stream().map(document -> {
            try {
                byte[] file = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(document.getId()).build()).readAllBytes();
                return new DocumentWithFile(document, file);
            } catch (Exception e) {
                log.error("Error fetching file for document ID {}: {}", document.getId(), e.getMessage(), e);
                return new DocumentWithFile(document, null);
            }
        }).collect(Collectors.toList());
    }

    private void sendOCRJobMessage(String documentId) {
        String message = "{\"documentId\":\"" + documentId + "\"}";
        rabbitTemplate.convertAndSend("OCR_QUEUE", message);
        log.info("Message sent to OCR queue for document ID: {}", documentId);
    }

    public Document getDocumentById(String id) {
        return documentRepository.findById(id).orElse(null);
    }
}
