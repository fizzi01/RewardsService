package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedeemRewardDTO {
    private String redeemId;
    private String rewardId;
    private String userEmail;
    private int quantity;
}
