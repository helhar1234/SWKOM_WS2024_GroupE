package at.technikum.worker.service;

import at.technikum.worker.rabbitMQ.RabbitMQConfig;
import at.technikum.worker.rabbitMQ.RabbitMQSender;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class ProcessingService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private OCRService ocrService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @RabbitListener(queues = RabbitMQConfig.PROCESSING_QUEUE)
    public void processOcrJob(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            System.out.println(jsonMessage);
            String documentId = jsonMessage.getString("documentId");
            System.out.println("Processing document with ID: " + documentId);

            // Fetch the document from MinIO
            InputStream documentStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket("documents").object(documentId).build());

            File tempFile = new File("/tmp/" + documentId + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = documentStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Perform OCR
            String ocrText = ocrService.extractText(tempFile);
            System.out.println("OCR Output: \n" + ocrText);

            // Send result to result_queue
            rabbitMQSender.sendToResultQueue(documentId, ocrText);

        } catch (Exception e) {
            System.err.println("Error processing OCR job: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
