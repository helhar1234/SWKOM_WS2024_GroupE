package at.technikum.paperlessrest.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OCR_QUEUE = "ocr_queue";

    @Bean
    public Queue ocrQueue() {
        return new Queue(OCR_QUEUE, true);
    }
}

