package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.jwt.JwtAuthenticationFilter;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private SecurityUtil securityUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CARD_NUMBER = "1234567812345678";
    private static final String MASKED = "**** **** **** 5678";


    @Test
    void create_shouldReturnCardDto() throws Exception {
        CreateCardRequest request = new CreateCardRequest(
                CARD_NUMBER,
                "JOHN DOE",
                YearMonth.of(2030, 12).atEndOfMonth(),
                1L
        );

        CardDto cardDto = CardDto.builder()
                .id(10L)
                .maskedNumber(MASKED)
                .owner("JOHN DOE")
                .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardService.create(any(CreateCardRequest.class)))
                .thenReturn(cardDto);

        mockMvc.perform(
                        post("/api/v1/cards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(cardService).create(any(CreateCardRequest.class));
    }

    @Test
    void getById_shouldReturnCard() throws Exception {
        CardDto cardDto = CardDto.builder()
                .id(10L)
                .maskedNumber(MASKED)
                .owner("JOHN DOE")
                .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardService.getById(10L))
                .thenReturn(cardDto);

        mockMvc.perform(get("/api/v1/cards/10"))
                .andExpect(status().isOk());

        verify(cardService).getById(10L);
    }

    @Test
    void getByUser_shouldReturnPage() throws Exception {
        Page<CardDto> page = new PageImpl<>(List.of(CardDto.builder().id(1L).build()));

        when(cardService.getByUser(eq(10L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/cards/user/10")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(cardService).getByUser(eq(10L), any(Pageable.class));
    }

    @Test
    void getMyCards_shouldUseCurrentUserId() throws Exception {
        Page<CardDto> page = new PageImpl<>(List.of(CardDto.builder().id(2L).build()));

        when(securityUtil.getCurrentUserId()).thenReturn(5L);
        when(cardService.getByUser(eq(5L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/cards/my")
                                .param("page", "0")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(securityUtil).getCurrentUserId();
        verify(cardService).getByUser(eq(5L), any(Pageable.class));
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        Page<CardDto> page = new PageImpl<>(List.of(CardDto.builder().id(3L).build()));

        when(cardService.getAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/cards")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(cardService).getAll(any(Pageable.class));
    }

    @Test
    void getByStatus_shouldReturnFilteredPage() throws Exception {
        Page<CardDto> page = new PageImpl<>(List.of(CardDto.builder().id(4L).build()));

        when(cardService.getByStatus(eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/cards/status")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(cardService)
                .getByStatus(eq(CardStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void getCardBalance_shouldReturnBalance() throws Exception {
        CardBalanceDto dto = CardBalanceDto.builder()
                .cardId(10L)
                .balance(BigDecimal.valueOf(100))
                .build();

        when(securityUtil.getCurrentUserId())
                .thenReturn(1L);

        when(cardService.getBalance(10L, 1L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/cards/my/10/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100));

        verify(cardService).getBalance(10L, 1L);
    }

    @Test
    void transfer_shouldCallService() throws Exception {
        TransferRequest request =
                new TransferRequest(10L, 20L, new BigDecimal("10"));

        when(securityUtil.getCurrentUserId())
                .thenReturn(1L);

        mockMvc.perform(
                        post("/api/v1/cards/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(transferService)
                .transfer(eq(1L), any(TransferRequest.class));
    }

    @Test
    void updateStatus_shouldReturnUpdatedCard() throws Exception {
        UpdateCardStatusRequest request = new UpdateCardStatusRequest(CardStatus.BLOCKED);
        CardDto cardDto = CardDto.builder()
                .id(10L)
                .maskedNumber(MASKED)
                .owner("JOHN DOE")
                .expirationDate(YearMonth.of(2030, 12).atEndOfMonth())
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardService.updateStatus(eq(10L), any(UpdateCardStatusRequest.class)))
                .thenReturn(cardDto);

        mockMvc.perform(
                        put("/api/v1/cards/10/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(cardService)
                .updateStatus(eq(10L), any());
    }

    @Test
    void delete_shouldCallService() throws Exception {
        mockMvc.perform(delete("/api/v1/cards/9"))
                .andExpect(status().isNoContent());

        verify(cardService).delete(9L);
    }

}
