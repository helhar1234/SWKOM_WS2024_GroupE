package at.technikum.worker.rabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import at.technikum.worker.service.*;

public class RabbitMQReceiver {

    @RabbitListener(queues = "${spring.rabbitmq.stream.name}")
    public void handleMessage(String documentPath) {
        System.out.println("Dokument empfangen: " + documentPath);

        // Start OCR-Service
        OCRService.processDocument(documentPath);
    }
}