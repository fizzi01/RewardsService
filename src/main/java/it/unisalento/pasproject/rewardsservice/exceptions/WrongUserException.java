package it.unisalento.pasproject.rewardsservice.exceptions;

import org.springframework.http.HttpStatus;

public class WrongUserException extends CustomErrorException{

    public WrongUserException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
