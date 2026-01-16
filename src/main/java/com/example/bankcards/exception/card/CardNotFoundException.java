package com.example.bankcards.exception.card;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class CardNotFoundException extends ApiException {

    public CardNotFoundException(Long cardId) {
        super("Card not found with id: " + cardId, HttpStatus.NOT_FOUND);
    }
}
