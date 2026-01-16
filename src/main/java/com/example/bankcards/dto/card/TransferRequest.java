package com.example.bankcards.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransferRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
