package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListRedeemDTO {
    private List<RedeemDTO> redeems;
}
