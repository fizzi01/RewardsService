package it.unisalento.pasproject.rewardsservice.service;

import it.unisalento.pasproject.rewardsservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.rewardsservice.dto.NotificationMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationMessageHandler {
    private final MessageProducer messageProducer;

    @Value("${rabbitmq.exchange.notification.name}")
    private String notificationExchange;

    @Value("${rabbitmq.routing.notification.key}")
    private String notificationRoutingKey;

    @Autowired
    public NotificationMessageHandler(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public void sendNotificationMessage(NotificationMessageDTO message) {
        messageProducer.sendMessage(message, notificationRoutingKey, notificationExchange);
    }
}
