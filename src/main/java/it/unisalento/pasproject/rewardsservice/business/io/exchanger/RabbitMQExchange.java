package it.unisalento.pasproject.rewardsservice.business.io.exchanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service("RabbitMQExchange")
public class RabbitMQExchange implements MessageExchangeStrategy {


    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQExchange(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQExchange.class);



    @Override
    public <T> T exchangeMessage(String message, String routingKey, String exchange, Class<T> object) {
        rabbitTemplate.setReplyTimeout(1000); // Timeout di 1 secondo
        T response = rabbitTemplate.convertSendAndReceiveAsType(exchange, routingKey, message,
                ParameterizedTypeReference.forType(object));
        LOGGER.info("Message received: {}", response);
        return response;
    }

    @Override
    public <T, R> R exchangeMessage(T message, String routingKey, String exchange, Class<R> responseType) {
        rabbitTemplate.setReplyTimeout(1000); // Timeout di 1 secondo
        R response = rabbitTemplate.convertSendAndReceiveAsType(exchange, routingKey, message,
                ParameterizedTypeReference.forType(responseType));
        LOGGER.info("Message received: {}", response);
        return response;
    }
}
