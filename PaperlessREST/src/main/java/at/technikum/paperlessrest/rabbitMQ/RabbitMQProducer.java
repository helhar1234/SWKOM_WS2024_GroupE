package at.technikum.paperlessrest.rabbitMQ;

import at.technikum.paperlessrest.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendToQueue(String documentPath) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, documentPath);
    }
}

