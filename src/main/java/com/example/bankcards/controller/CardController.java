package com.example.bankcards.controller;

import com.example.bankcards.config.swagger.errors.CommonApiErrors;
import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(
        name = "Cards",
        description = "Управление банковскими картами, балансами и переводами между картами"
)
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(
        value = "/api/v1/cards",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class CardController {

    private final CardService cardService;
    private final TransferService transferService;
    private final SecurityUtil securityUtil;


    @Operation(
            summary = "Создать банковскую карту",
            description = "Создание новой банковской карты для пользователя. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true, cardConflict = true)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> create(@RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.create(request));
    }


    @Operation(
            summary = "Получить карты пользователя",
            description = "Получение всех карт конкретного пользователя. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт пользователя"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true)
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getByUser(@PathVariable Long userId,
                                   @ParameterObject
                                   @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
                                   Pageable pageable) {
        return cardService.getByUser(userId, pageable);
    }

    @Operation(
            summary = "Получить карту по ID",
            description = "Получение банковской карты по идентификатору. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта найдена"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true)
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public CardDto getById(@PathVariable Long id) {
        return cardService.getById(id);
    }

    @Operation(
            summary = "Получить мои карты",
            description = "Получение списка карт текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт пользователя")
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Page<CardDto> getMyCards(@ParameterObject
                                    @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
                                    Pageable pageable) {
        Long userId = securityUtil.getCurrentUserId();
        return cardService.getByUser(userId, pageable);
    }

    @Operation(
            summary = "Получить все карты",
            description = "Постраничное получение всех карт в системе. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт"),
    })
    @CommonApiErrors(forbidden = true)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getAll(@ParameterObject
                                @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
                                Pageable pageable) {
        return cardService.getAll(pageable);
    }

    @Operation(
            summary = "Получить карты по статусу",
            description = "Получение списка карт по статусу (ACTIVE, BLOCKED и т.д.). Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт по статусу"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true)
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardDto> getByStatus(
            @RequestParam CardStatus status,
            @ParameterObject
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return cardService.getByStatus(status, pageable);
    }


    @Operation(
            summary = "Получить баланс карты",
            description = "Получение баланса конкретной карты текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс карты"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true)
    @GetMapping("/my/{cardId:\\d+}/balance")
    @PreAuthorize("hasRole('USER')")
    public CardBalanceDto getCardBalance(@PathVariable Long cardId) {
        Long userId = securityUtil.getCurrentUserId();
        return cardService.getBalance(cardId, userId);
    }

    @Operation(
            summary = "Установить баланс карты",
            description = "Принудительное изменение баланса карты. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс успешно изменен"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true)
    @PutMapping("/{cardId:\\d+}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    public CardBalanceDto setCardBalance(
            @PathVariable Long cardId,
            @RequestParam BigDecimal amount
    ) {
        return cardService.setBalance(cardId, amount);
    }

    @Operation(
            summary = "Получить общий баланс",
            description = "Получение суммарного баланса всех карт текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Общий баланс пользователя")
    })
    @GetMapping("/my/balance")
    @PreAuthorize("hasRole('USER')")
    public CardBalanceDto getTotalBalance() {
        Long userId = securityUtil.getCurrentUserId();

        return CardBalanceDto.builder()
                .cardId(null)
                .balance(cardService.getTotalBalance(userId))
                .build();
    }


    @Operation(
            summary = "Перевод между картами",
            description = "Перевод средств между картами текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true, methodNotAllowed = true)
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public void transfer(@RequestBody TransferRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        transferService.transfer(userId, request);
    }


    @Operation(
            summary = "Изменить статус карты",
            description = "Изменение статуса карты (ACTIVE, BLOCKED и т.д.). Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус карты изменен"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true)
    @PutMapping("/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public CardDto updateStatus(
            @PathVariable Long cardId,
            @RequestBody UpdateCardStatusRequest request
    ) {
        return cardService.updateStatus(cardId, request);
    }


    @Operation(
            summary = "Удалить карту",
            description = "Удаление банковской карты по ID. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
    })
    @CommonApiErrors(forbidden = true, cardNotFound = true)
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long cardId) {
        cardService.delete(cardId);
        return ResponseEntity.noContent().build();
    }
}
