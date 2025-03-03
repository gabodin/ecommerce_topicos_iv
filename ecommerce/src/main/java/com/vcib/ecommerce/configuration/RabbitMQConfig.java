package com.vcib.ecommerce.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	public static final String EXCHANGE_NAME = "fidelity-exchange";
    public static final String QUEUE_NAME = "fidelity-queue";
    public static final String ROUTING_KEY = "fidelity-routing-key";
    
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
    
    
    @Bean
    public Queue queue() {
    	return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-message-ttl", 60000)
                .build();
    }
    
    @Bean
    public DirectExchange exchange() {
    	return new DirectExchange(EXCHANGE_NAME);
    }
    
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
    	return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}
