package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RedeemTransactionDTO {
    private String senderEmail; // user email
    private String receiverEmail;   // reward id
    private double amount;
    private String description;

    private String transactionOwner;

    private LocalDateTime creationDate;
    private LocalDateTime completionDate;

    private boolean isCompleted;
}
