package at.technikum.paperlessrest.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String documentId) {
        // Format message as JSON
        String jsonMessage = String.format("{\"documentId\": \"%s\"}", documentId);

        // Log the message for debugging
        System.out.println("Sending message to processing queue: " + jsonMessage);

        // Send to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, jsonMessage);
    }
}
