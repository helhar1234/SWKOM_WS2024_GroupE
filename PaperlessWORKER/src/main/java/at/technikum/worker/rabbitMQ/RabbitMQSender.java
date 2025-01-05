package at.technikum.worker.rabbitMQ;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        String jsonMessage = String.format("{\"documentId\": \"%s\"}", documentId);
        rabbitTemplate.convertAndSend(processingQueue, jsonMessage);
    }

    public void sendToResultQueue(String documentId, String ocrText) {
        String jsonMessage = String.format("{\"documentId\": \"%s\", \"ocrText\": \"%s\"}", documentId, ocrText);
        rabbitTemplate.convertAndSend(resultQueue, jsonMessage);
    }
}
