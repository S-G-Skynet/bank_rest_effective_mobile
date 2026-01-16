package com.example.bankcards.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class CreateCardRequest {

    private String cardNumber;

    private String owner;

    private LocalDate expirationDate;

    private Long userId;
}
