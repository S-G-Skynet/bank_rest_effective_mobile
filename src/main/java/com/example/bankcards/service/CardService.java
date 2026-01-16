package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardBalanceDto;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.card.UpdateCardStatusRequest;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;


public interface CardService {

    CardBalanceDto getBalance(Long cardId, Long userId);
    CardBalanceDto setBalance(Long cardId, BigDecimal amount);

    BigDecimal getTotalBalance(Long userId);

    CardDto create(CreateCardRequest request);

    CardDto getById(Long id);

    Page<CardDto> getByUser(Long userId, Pageable pageable);

    Page<CardDto> getAll(Pageable pageable);

    Page<CardDto> getByStatus(CardStatus status, Pageable pageable);

    CardDto updateStatus(Long cardId, UpdateCardStatusRequest request);

    void delete(Long cardId);
}
