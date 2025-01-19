package at.technikum.paperlessrest.rabbitmq;

import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RabbitMQResultListener {

    private final DocumentRepository documentRepository;

    public RabbitMQResultListener(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE)
    public void handleOcrResult(String message) {
        log.info("Received OCR result message: {}", message);
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String documentId = jsonMessage.getString("documentId");

            // Find document by ID and update ocrJobDone
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

            document.setOcrJobDone(true);
            documentRepository.save(document);

            log.info("OCR processing completed for document ID: {}. Updated 'ocrJobDone' to true.", documentId);
        } catch (Exception e) {
            log.error("Failed to process OCR result message: {}", e.getMessage(), e);
        }
    }

}
