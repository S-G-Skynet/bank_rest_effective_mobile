package com.example.bankcards.service;


import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.card.AccessDeniedException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("john")
                .build();

        fromCard = Card.builder()
                .id(10L)
                .user(user)
                .balance(new BigDecimal("100.00"))
                .build();

        toCard = Card.builder()
                .id(20L)
                .user(user)
                .balance(new BigDecimal("50.00"))
                .build();
    }

    @Test
    void transfer_shouldThrowException_whenFromCardNotFound() {
        TransferRequest request =
                new TransferRequest(10L, 20L, new BigDecimal("10"));

        when(cardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                CardNotFoundException.class,
                () -> transferService.transfer(1L, request)
        );
    }

    @Test
    void transfer_shouldThrowException_whenToCardNotFound() {
        TransferRequest request =
                new TransferRequest(10L, 20L, new BigDecimal("10"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(
                CardNotFoundException.class,
                () -> transferService.transfer(1L, request)
        );
    }

    @Test
    void transfer_shouldThrowException_whenUserIsNotOwnerOfCards() {
        User anotherUser = User.builder()
                .id(2L)
                .username("evil")
                .build();

        fromCard.setUser(anotherUser);

        TransferRequest request =
                new TransferRequest(10L, 20L, new BigDecimal("10"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));

        assertThrows(
                AccessDeniedException.class,
                () -> transferService.transfer(1L, request)
        );
    }

    @Test
    void transfer_shouldThrowException_whenInsufficientFunds() {
        TransferRequest request =
                new TransferRequest(10L, 20L, new BigDecimal("1000"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));

        assertThrows(
                InsufficientFundsException.class,
                () -> transferService.transfer(1L, request)
        );
    }

    @Test
    void transfer_shouldUpdateBalances_whenSuccess() {
        TransferRequest request =
                new TransferRequest(10L, 20L, new BigDecimal("30"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));

        transferService.transfer(1L, request);

        assertEquals(new BigDecimal("70.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("80.00"), toCard.getBalance());
    }
}

