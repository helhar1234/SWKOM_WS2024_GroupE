package at.technikum.paperlessrest.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "document_processing_queue";
    public static final String EXCHANGE = "document_exchange";
    public static final String ROUTING_KEY = "document_routing_key";

    public static final String RESULT_QUEUE = "document_result_queue";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Queue result_queue() {
        return new Queue(RESULT_QUEUE, true);
    }


    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}
