package it.unisalento.pasproject.rewardsservice.business.io.consumer;

public interface MessageConsumerStrategy {
    <T> T consumeMessage(T message);
    String consumeMessage(String message, String queueName);
}
