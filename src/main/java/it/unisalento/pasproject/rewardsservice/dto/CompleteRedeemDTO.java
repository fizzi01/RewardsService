package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteRedeemDTO {
    private String userEmail;
    private String rewardId;
    private String redeemCode;
}
