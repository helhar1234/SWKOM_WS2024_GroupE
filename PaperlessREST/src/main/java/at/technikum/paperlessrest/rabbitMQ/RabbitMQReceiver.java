package at.technikum.paperlessrest.rabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import at.technikum.paperlessrest.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j; //why do i need this??

@Slf4j
public class RabbitMQReceiver {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveMessage(String message) {
        log.info("Nachricht empfangen: " + message);
    }
}
