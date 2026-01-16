package com.example.bankcards.exception.card;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class CardAlreadyExistsException extends ApiException {

    public CardAlreadyExistsException() {
        super("Card already exists", HttpStatus.CONFLICT);
    }
}
