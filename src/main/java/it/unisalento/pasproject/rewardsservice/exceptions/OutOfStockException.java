package it.unisalento.pasproject.rewardsservice.exceptions;

import org.springframework.http.HttpStatus;

public class OutOfStockException extends CustomErrorException{

    public OutOfStockException(String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }
}
