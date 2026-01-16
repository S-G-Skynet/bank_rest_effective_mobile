package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final TransferService transferService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CardDto create(@RequestBody CreateCardRequest request) {
        return cardService.create(request);
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public CardDto getById(@PathVariable Long id) {
        return cardService.getById(id);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getByUser(@PathVariable Long userId, Pageable pageable) {
        return cardService.getByUser(userId, pageable);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Page<CardDto> getMyCards(Pageable pageable) {
        Long userId = securityUtil.getCurrentUserId();
        return cardService.getByUser(userId, pageable);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getAll(Pageable pageable) {
        return cardService.getAll(pageable);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getByStatus(
            @RequestParam CardStatus status,
            Pageable pageable
    ) {
        return cardService.getByStatus(status, pageable);
    }


    @GetMapping("/my/{cardId:\\d+}/balance")
    @PreAuthorize("hasRole('USER')")
    public CardBalanceDto getCardBalance(@PathVariable Long cardId) {
        Long userId = securityUtil.getCurrentUserId();
        return cardService.getBalance(cardId, userId);
    }

    @PutMapping("/{cardId:\\d+}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    public CardBalanceDto setCardBalance(
            @PathVariable Long cardId,
            @RequestParam BigDecimal amount) {
        return cardService.setBalance(cardId, amount);
    }

    @GetMapping("/my/balance")
    @PreAuthorize("hasRole('USER')")
    public CardBalanceDto getTotalBalance() {
        Long userId = securityUtil.getCurrentUserId();

        return CardBalanceDto.builder()
                .cardId(null)
                .balance(cardService.getTotalBalance(userId))
                .build();
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public void transfer(@RequestBody TransferRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        transferService.transfer(userId, request);
    }

    @PutMapping("/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public CardDto updateStatus(
            @PathVariable Long cardId,
            @RequestBody UpdateCardStatusRequest request
    ) {
        return cardService.updateStatus(cardId, request);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long cardId) {
        cardService.delete(cardId);
        return ResponseEntity.noContent().build();
    }
}
