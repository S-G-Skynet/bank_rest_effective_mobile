package com.example.bankcards.service;

import com.example.bankcards.config.mapper.CardMapper;
import com.example.bankcards.dto.card.CardBalanceDto;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.card.UpdateCardStatusRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.card.CardAlreadyExistsException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private Card card;

    private static final String CARD_NUMBER = "1234567812345678";
    private static final String ENCRYPTED = "encrypted";
    private static final String MASKED = "**** **** **** 5678";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("john")
                .build();

        card = Card.builder()
                .id(10L)
                .encryptedCardNumber(ENCRYPTED)
                .owner("JOHN DOE")
                .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .user(user)
                .build();
    }

    @Test
    void create_shouldSaveCard_whenSuccess() {
        CreateCardRequest request = new CreateCardRequest(
                CARD_NUMBER,
                "JOHN DOE",
                YearMonth.of(2030, 12).atEndOfMonth(),
                1L
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoService.encrypt(CARD_NUMBER)).thenReturn(ENCRYPTED);
        when(cardRepository.existsByEncryptedCardNumber(ENCRYPTED)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardDto dto = CardDto.builder()
                .id(10L)
                .maskedNumber(MASKED)
                .owner("JOHN DOE")
                .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardMapper.toDto(card, MASKED)).thenReturn(dto);

        CardDto result = cardService.create(request);

        assertEquals(MASKED, result.getMaskedNumber());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());
    }

    @Test
    void create_shouldThrowException_whenUserNotFound() {
        CreateCardRequest request = new CreateCardRequest(
                CARD_NUMBER,
                "JOHN DOE",
                YearMonth.of(2030, 12).atEndOfMonth(),
                1L
        );

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> cardService.create(request)
        );
    }

    @Test
    void getById_shouldReturnCard_whenSuccess() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cryptoService.decrypt(ENCRYPTED)).thenReturn(CARD_NUMBER);

        CardDto dto = CardDto.builder()
                .id(10L)
                .maskedNumber(MASKED)
                .owner("JOHN DOE")
                .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .build();

        when(cardMapper.toDto(card, MASKED)).thenReturn(dto);

        CardDto result = cardService.getById(10L);

        assertEquals(MASKED, result.getMaskedNumber());
        assertEquals(new BigDecimal("100.00"), result.getBalance());
    }

    @Test
    void create_shouldThrowException_whenCardAlreadyExists() {
        CreateCardRequest request = new CreateCardRequest(
                CARD_NUMBER,
                "JOHN DOE",
                YearMonth.of(2030, 12).atEndOfMonth(),
                1L
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoService.encrypt(CARD_NUMBER)).thenReturn(ENCRYPTED);
        when(cardRepository.existsByEncryptedCardNumber(ENCRYPTED)).thenReturn(true);

        assertThrows(
                CardAlreadyExistsException.class,
                () -> cardService.create(request)
        );
    }


    @Test
    void getById_shouldThrowException_whenCardNotFound() {
        when(cardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                CardNotFoundException.class,
                () -> cardService.getById(10L)
        );
    }

    @Test
    void getByUser_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findAllByUserId(1L, pageable)).thenReturn(page);
        when(cryptoService.decrypt(ENCRYPTED)).thenReturn(CARD_NUMBER);

        when(cardMapper.toDto(card, MASKED))
                .thenReturn(CardDto.builder()
                        .id(10L)
                        .maskedNumber(MASKED)
                        .owner("JOHN DOE")
                        .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                        .status(CardStatus.ACTIVE)
                        .balance(new BigDecimal("100.00"))
                        .build());

        Page<CardDto> result = cardService.getByUser(1L, pageable);

        assertEquals(1, result.getTotalElements());
    }


    @Test
    void getAll_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findAll(pageable)).thenReturn(page);
        when(cryptoService.decrypt(ENCRYPTED)).thenReturn(CARD_NUMBER);
        when(cardMapper.toDto(card, MASKED)).thenReturn(
                CardDto.builder()
                        .id(10L)
                        .maskedNumber(MASKED)
                        .owner("JOHN DOE")
                        .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                        .status(CardStatus.ACTIVE)
                        .balance(new BigDecimal("100.00"))
                        .build()
        );

        Page<CardDto> result = cardService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
    }


    @Test
    void getByStatus_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findAllByStatus(CardStatus.ACTIVE, pageable)).thenReturn(page);
        when(cryptoService.decrypt(ENCRYPTED)).thenReturn(CARD_NUMBER);
        when(cardMapper.toDto(card, MASKED)).thenReturn(
                CardDto.builder()
                        .id(10L)
                        .maskedNumber(MASKED)
                        .owner("JOHN DOE")
                        .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                        .status(CardStatus.ACTIVE)
                        .balance(new BigDecimal("100.00"))
                        .build()
        );

        Page<CardDto> result = cardService.getByStatus(CardStatus.ACTIVE, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBalance_shouldReturnBalance() {
        when(cardRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(card));

        CardBalanceDto dto = cardService.getBalance(10L, 1L);

        assertEquals(10L, dto.getCardId());
        assertEquals(new BigDecimal("100.00"), dto.getBalance());
    }

    @Test
    void getBalance_shouldThrowException_whenCardNotFound() {
        when(cardRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                CardNotFoundException.class,
                () -> cardService.getBalance(10L, 1L)
        );
    }

    @Test
    void getTotalBalance_shouldReturnSum() {
        when(cardRepository.sumBalanceByUserId(1L))
                .thenReturn(new BigDecimal("300.00"));

        BigDecimal result = cardService.getTotalBalance(1L);

        assertEquals(new BigDecimal("300.00"), result);
    }

    @Test
    void updateStatus_shouldUpdateStatus() {
        UpdateCardStatusRequest request =
                new UpdateCardStatusRequest(CardStatus.BLOCKED);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cryptoService.decrypt(ENCRYPTED)).thenReturn(CARD_NUMBER);

        when(cardMapper.toDto(card, MASKED))
                .thenReturn(CardDto.builder()
                        .id(10L)
                        .maskedNumber(MASKED)
                        .owner("JOHN DOE")
                        .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                        .status(CardStatus.BLOCKED)
                        .balance(new BigDecimal("100.00"))
                        .build());

        CardDto result = cardService.updateStatus(10L, request);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
    }

    @Test
    void updateStatus_shouldThrowException_whenCardNotFound() {
        when(cardRepository.findById(10L)).thenReturn(Optional.empty());

        UpdateCardStatusRequest request =
                new UpdateCardStatusRequest(CardStatus.BLOCKED);

        assertThrows(
                CardNotFoundException.class,
                () -> cardService.updateStatus(10L, request)
        );
    }

    @Test
    void delete_shouldThrowException_whenCardNotFound() {
        when(cardRepository.existsById(10L)).thenReturn(false);

        assertThrows(
                CardNotFoundException.class,
                () -> cardService.delete(10L)
        );
    }

    @Test
    void delete_shouldDeleteCard() {
        when(cardRepository.existsById(10L)).thenReturn(true);

        cardService.delete(10L);

        verify(cardRepository).deleteById(10L);
    }
}

