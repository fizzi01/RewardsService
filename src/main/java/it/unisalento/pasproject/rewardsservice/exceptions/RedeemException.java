package it.unisalento.pasproject.rewardsservice.exceptions;

import org.springframework.http.HttpStatus;

public class RedeemException extends CustomErrorException{
    public RedeemException(String message) {
        super(message, HttpStatus.EXPECTATION_FAILED);
    }
}
