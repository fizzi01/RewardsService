package it.unisalento.pasproject.rewardsservice.exceptions;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends CustomErrorException{
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
