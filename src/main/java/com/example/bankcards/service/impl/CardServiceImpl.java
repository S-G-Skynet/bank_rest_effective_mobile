package com.example.bankcards.service.impl;

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
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CryptoService cryptoService;

    @Override
    @Transactional
    public CardDto create(CreateCardRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        String encrypted = cryptoService.encrypt(request.getCardNumber());

        if (cardRepository.existsByEncryptedCardNumber(encrypted)) {
            throw new CardAlreadyExistsException();
        }

        Card card = Card.builder()
                .encryptedCardNumber(encrypted)
                .owner(request.getOwner())
                .expirationDate(request.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        Card saved = cardRepository.save(card);

        return cardMapper.toDto(saved, mask(request.getCardNumber()));
    }


    @Override
    public CardDto getById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        String decrypted = cryptoService.decrypt(card.getEncryptedCardNumber());
        return cardMapper.toDto(card, mask(decrypted));
    }


    @Override
    public Page<CardDto> getByUser(Long userId, Pageable pageable) {

        return cardRepository.findAllByUserId(userId, pageable)
                .map(card -> {
                    String decrypted = cryptoService.decrypt(card.getEncryptedCardNumber());
                    return cardMapper.toDto(card, mask(decrypted));
                });
    }

    @Override
    public Page<CardDto> getAll(Pageable pageable) {

        return cardRepository.findAll(pageable)
                .map(card -> {
                    String decrypted = cryptoService.decrypt(card.getEncryptedCardNumber());
                    return cardMapper.toDto(card, mask(decrypted));
                });
    }

    @Override
    public Page<CardDto> getByStatus(CardStatus status, Pageable pageable) {

        return cardRepository.findAllByStatus(status, pageable)
                .map(card -> {
                    String decrypted = cryptoService.decrypt(card.getEncryptedCardNumber());
                    return cardMapper.toDto(card, mask(decrypted));
                });
    }

    @Override
    public CardBalanceDto getBalance(Long cardId, Long userId) {

        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        return CardBalanceDto.builder()
                .cardId(card.getId())
                .balance(card.getBalance())
                .build();
    }

    @Override
    @Transactional
    public CardBalanceDto setBalance(Long cardId, BigDecimal balance) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        card.setBalance(card.getBalance().add(balance));
        Card savedCard = cardRepository.save(card);

        return  CardBalanceDto.builder()
                .cardId(savedCard.getId())
                .balance(savedCard.getBalance())
                .build();
    }

    @Override
    public BigDecimal getTotalBalance(Long userId) {
        return cardRepository.sumBalanceByUserId(userId);
    }


    @Override
    public CardDto updateStatus(Long cardId, UpdateCardStatusRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        card.setStatus(request.getStatus());
        Card saved = cardRepository.save(card);

        String decrypted = cryptoService.decrypt(saved.getEncryptedCardNumber());
        return cardMapper.toDto(saved, mask(decrypted));
    }

    @Override
    public void delete(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }
        cardRepository.deleteById(cardId);
    }

    private String mask(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
