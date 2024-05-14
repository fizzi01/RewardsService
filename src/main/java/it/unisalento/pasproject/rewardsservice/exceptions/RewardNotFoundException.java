package it.unisalento.pasproject.rewardsservice.exceptions;

import org.springframework.http.HttpStatus;

public class RewardNotFoundException extends CustomErrorException{
    public RewardNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
