package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RedeemDTO {
    private String redeemId;
    private String rewardId;
    private String userEmail;
    private int quantity;
    private LocalDateTime redeemDate;   // Data di riscatto del reward
    private boolean redeemed;  // True se viene completata la transazione

    private String redeemCode;  // Codice univoco per il riscatto
    private boolean used;   // True se il codice è stato usato
    private LocalDateTime usedDate;  // Data di utilizzo del codice
}
