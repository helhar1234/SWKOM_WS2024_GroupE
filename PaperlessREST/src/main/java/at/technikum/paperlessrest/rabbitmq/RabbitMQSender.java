package at.technikum.paperlessrest.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOCRJobMessage(String documentId) {
        String message = "{\"documentId\":\"" + documentId + "\"}";
        log.info("Preparing to send OCR job message for document ID: {}", documentId);

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
            log.info("Message successfully sent to RabbitMQ for document ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to send message to RabbitMQ for document ID: {}", documentId, e);
        }
    }
}
