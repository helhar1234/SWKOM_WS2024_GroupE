package at.technikum.worker.service;

import at.technikum.worker.rabbitMQ.RabbitMQConfig;
import at.technikum.worker.rabbitMQ.RabbitMQSender;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Slf4j
@Service
public class ProcessingService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private OCRService ocrService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @RabbitListener(queues = RabbitMQConfig.PROCESSING_QUEUE)
    public void processOcrJob(String message) {
        //log.info("Received message from processing queue: {}", message);

        try {
            JSONObject jsonMessage = new JSONObject(message);
            String documentId = jsonMessage.getString("documentId");
            String filename = jsonMessage.getString("filename");
            log.info("Processing OCR job for document ID: {}", documentId);

            // Fetch the document from MinIO
            log.info("Fetching document from MinIO for document ID: {}", documentId);
            InputStream documentStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket("documents").object(documentId).build());

            File tempFile = new File("/tmp/" + documentId + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = documentStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                log.info("File successfully downloaded to: {}", tempFile.getAbsolutePath());
            }

            // Perform OCR
            log.info("Starting OCR process for file: {}", tempFile.getName());
            String ocrText = ocrService.extractText(tempFile);
            log.info("OCR process completed for document ID: {}. Extracted text: {}"+ documentId+ ocrText);

            //Index Document for elastic
            elasticsearchService.indexDocument(documentId, filename, ocrText);

            // Send result to result_queue
            log.info("Sending OCR result to result queue for document ID: {}", documentId);
            rabbitMQSender.sendToResultQueue(documentId, ocrText);
            log.info("OCR result successfully sent to result queue.");

        } catch (Exception e) {
            log.error("Error processing OCR job: {}", e.getMessage(), e);
        }
    }
}
