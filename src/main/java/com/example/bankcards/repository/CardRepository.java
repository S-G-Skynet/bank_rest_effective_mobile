package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

@NullMarked
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findAllByUserId(Long userId, Pageable pageable);

    Optional<Card> findByIdAndUserId(Long id, Long userId);

    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);

    @Query("""
                select coalesce(sum(c.balance), 0)
                from Card c
                where c.user.id = :userId
            """)
    BigDecimal sumBalanceByUserId(Long userId);

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
}
