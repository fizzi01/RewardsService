package it.unisalento.pasproject.rewardsservice.domain;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "redeems")
public class Redeem {
    @Id
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
