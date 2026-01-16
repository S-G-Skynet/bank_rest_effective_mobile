package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateCardStatusRequest {

    private CardStatus status;
}
