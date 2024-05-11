package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class RedeemRewardDTO {
    private String redeemId;
    private String rewardId;
    private String userEmail;
    private int quantity;
}
