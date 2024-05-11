package it.unisalento.pasproject.rewardsservice.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "rewards")
public class Reward {
    @Id
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
