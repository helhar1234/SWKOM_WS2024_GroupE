package at.technikum.paperlessrest.producer;

import at.technikum.paperlessrest.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendToOCRQueue(String documentPath) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.OCR_QUEUE, documentPath);
    }
}

