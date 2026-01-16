package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {

    private Long id;

    private String maskedNumber;

    private String owner;

    private LocalDate expirationDate;

    private CardStatus status;

    private BigDecimal balance;
}
