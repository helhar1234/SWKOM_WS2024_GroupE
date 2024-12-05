package at.technikum.paperlessrest.rabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import at.technikum.paperlessrest.config.RabbitMQConfig;

public class RabbitMQReceiver {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveMessage(String message) {
        System.out.println(message);
        //log.info("Nachricht empfangen: {}", message);
    }
}
