package at.technikum.paperlessrest.rabbitmq;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RabbitMQSenderTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final RabbitMQSender rabbitMQSender = new RabbitMQSender(rabbitTemplate);

    @Test
    void sendOCRJobMessage_success() {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        String expectedMessage = "{\"documentId\":\"" + documentId + "\"}";

        // Act
        rabbitMQSender.sendOCRJobMessage(documentId);

        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate).convertAndSend(exchangeCaptor.capture(), routingKeyCaptor.capture(), messageCaptor.capture());

        assertEquals(RabbitMQConfig.EXCHANGE, exchangeCaptor.getValue());
        assertEquals(RabbitMQConfig.ROUTING_KEY, routingKeyCaptor.getValue());
        assertEquals(expectedMessage, messageCaptor.getValue());
    }

    @Test
    void sendOCRJobMessage_failure() {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        String expectedMessage = "{\"documentId\":\"" + documentId + "\"}";

        doThrow(new RuntimeException("RabbitMQ error")).when(rabbitTemplate)
                .convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, expectedMessage);

        // Act
        rabbitMQSender.sendOCRJobMessage(documentId);

        // Assert
        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, expectedMessage);
        // Der Fehler wird im Log protokolliert, aber nicht weitergegeben.
    }
}
