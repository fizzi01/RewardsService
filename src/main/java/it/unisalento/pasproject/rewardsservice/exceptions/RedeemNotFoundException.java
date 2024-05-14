package it.unisalento.pasproject.rewardsservice.exceptions;

import org.springframework.http.HttpStatus;

public class RedeemNotFoundException extends CustomErrorException{
    public RedeemNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
