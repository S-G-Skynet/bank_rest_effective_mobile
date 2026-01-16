package com.example.bankcards.config.mapper;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.entity.Card;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardMapper {

    public CardDto toDto(Card card, String maskedNumber) {
        if (card == null) {
            return null;
        }

        return CardDto.builder()
                .id(card.getId())
                .maskedNumber(maskedNumber)
                .owner(card.getOwner())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }
}
