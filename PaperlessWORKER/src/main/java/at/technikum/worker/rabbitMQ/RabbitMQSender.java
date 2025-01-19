package at.technikum.worker.rabbitMQ;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.processing.queue}")
    private String processingQueue;

    @Value("${rabbitmq.result.queue}")
    private String resultQueue;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendToProcessingQueue(String documentId) {
        String jsonMessage = String.format("{\"documentId\": \"%s\"}"+ documentId);
        //log.info("Preparing to send message to processing queue: {}", jsonMessage);

        try {
            rabbitTemplate.convertAndSend(processingQueue, jsonMessage);
            //log.info("Message successfully sent to processing queue: {}", jsonMessage);
        } catch (Exception e) {
            log.error("Failed to send message to processing queue: {}", jsonMessage, e);
        }
    }

    public void sendToResultQueue(String documentId, String ocrText) {
        String message = "{\"documentId\":\"" + documentId + "\"}";
        try {
            rabbitTemplate.convertAndSend(resultQueue, message);
            log.info("Message successfully sent to RabbitMQ for document ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to send message to RabbitMQ for document ID: {}", documentId, e);
        }
    }


}
