package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListRewardDTO {
    private List<RewardDTO> rewards;
}
