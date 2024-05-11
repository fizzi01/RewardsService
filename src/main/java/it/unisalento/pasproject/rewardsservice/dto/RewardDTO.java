package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RewardDTO {
    private String id;
    private String name;
    private double cost;
    private String description;
    private String image;
    private String category;
    private String subcategory;
    private LocalDateTime addDate;
    private boolean active;
    private int quantity;
    private int sold;
}
