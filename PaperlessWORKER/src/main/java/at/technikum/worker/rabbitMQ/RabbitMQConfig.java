package at.technikum.worker.rabbitMQ;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PROCESSING_QUEUE = "document_processing_queue";
    public static final String RESULT_QUEUE = "document_result_queue";

    @Bean
    public Queue processingQueue() {
        return new Queue(PROCESSING_QUEUE);
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(RESULT_QUEUE);
    }
}
