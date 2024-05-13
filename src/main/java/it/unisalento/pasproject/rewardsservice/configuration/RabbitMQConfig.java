package it.unisalento.pasproject.rewardsservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ------  SECURITY  ------ //

    // Needed by authentication service
    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;


    @Bean
    public TopicExchange securityExchange() {
        return new TopicExchange(securityExchange);
    }


    // ------  END SECURITY  ------ //

    // ------  REDEEM  ------ //

    //rabbitmq.routing.sendTransaction.name=transaction.receive
    //rabbitmq.exchange.transaction.name=transaction-exchange

    @Value("${rabbitmq.exchange.transaction.name}")
    private String transactionExchange;

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(transactionExchange);
    }


    @Value("${rabbitmq.routing.notifyTransaction.name}")
    private String notifyTransactionRoutingKey;

    @Value("${rabbitmq.queue.notifyTransaction.name}")
    private String notifyTransactionQueue;

    @Bean
    public Queue notifyTransactionQueue() {
        return QueueBuilder.durable(notifyTransactionQueue).build();
    }

    @Bean
    public Binding notifyTransactionBinding() {
        return BindingBuilder
                .bind(notifyTransactionQueue())
                .to(transactionExchange())
                .with(notifyTransactionRoutingKey);
    }

    // ------  END REDEEM  ------ //

    // ----- WALLET CREATION ----- //

    @Value("${rabbitmq.routing.sendRewardData.key}")
    private String sendRewardDataRoutingKey;

    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;

    @Bean
    public TopicExchange dataExchange() {
        return new TopicExchange(dataExchange);
    }




    /**
     * Creates a message converter for JSON messages.
     *
     * @return a new Jackson2JsonMessageConverter instance.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates an AMQP template for sending messages.
     *
     * @param connectionFactory the connection factory to use.
     * @return a new RabbitTemplate instance.
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
