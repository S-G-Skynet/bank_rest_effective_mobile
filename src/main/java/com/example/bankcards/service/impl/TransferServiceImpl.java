package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.card.AccessDeniedException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;

    @Override
    public void transfer(Long userId, TransferRequest request) {

        Card from = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException(request.getFromCardId()));

        Card to = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException(request.getToCardId()));

        if (!from.getUser().getId().equals(userId)
                || !to.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(userId);
        }

        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot to transfer when card is not active");
        }

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));
    }
}

