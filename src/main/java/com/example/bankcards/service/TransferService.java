package com.example.bankcards.service;

import com.example.bankcards.dto.card.TransferRequest;

public interface TransferService {
    void transfer(Long userId, TransferRequest request);
}

