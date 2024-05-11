package it.unisalento.pasproject.rewardsservice.business.io.exchanger;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Setter
public class MessageExchanger {
    
    
    private MessageExchangeStrategy strategy;

    @Autowired
    public MessageExchanger(MessageExchangeStrategy messageExchangeStrategy) {
        this.strategy = messageExchangeStrategy;
    }

    public <T> T exchangeMessage(String message, String routingKey,String exchange, Class<T> responseType) {
        return strategy.exchangeMessage(message, routingKey, exchange,responseType);
    }

    public <T, R> R exchangeMessage(T message, String routingKey, String exchange, Class<R> responseType) {
        return strategy.exchangeMessage(message, routingKey, exchange, responseType);
    }
}
