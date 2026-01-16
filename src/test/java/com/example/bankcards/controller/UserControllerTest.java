package com.example.bankcards.controller;

import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.security.jwt.JwtAuthenticationFilter;
import com.example.bankcards.service.UserService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SecurityUtil securityUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createClient_shouldReturnUserDto() throws Exception {
        CreateUserRequest request =
                new CreateUserRequest("client", "password");

        UserDto dto = UserDto.builder()
                .id(1L)
                .username("client")
                .role(UserRole.USER)
                .build();

        when(userService.create(any(), eq(UserRole.USER)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/v1/users/create/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("client"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).create(any(), eq(UserRole.USER));
    }

    @Test
    void createAdmin_shouldReturnUserDto() throws Exception {
        CreateUserRequest request =
                new CreateUserRequest("admin", "password");

        UserDto dto = UserDto.builder()
                .id(2L)
                .username("admin")
                .role(UserRole.ADMIN)
                .build();

        when(userService.create(any(), eq(UserRole.ADMIN)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/v1/users/create/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).create(any(), eq(UserRole.ADMIN));
    }

    @Test
    void getById_shouldReturnUser() throws Exception {
        UserDto dto = UserDto.builder()
                .id(10L)
                .username("user10")
                .role(UserRole.USER)
                .build();

        when(userService.getById(10L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("user10"));

        verify(userService).getById(10L);
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        Page<UserDto> page = new PageImpl<>(
                List.of(UserDto.builder().id(1L).build())
        );

        when(userService.getAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(userService).getAll(any(Pageable.class));
    }

    @Test
    void updateUsernameByAdmin_shouldReturnUpdatedUser() throws Exception {
        ChangeUserUsernameRequest request =
                new ChangeUserUsernameRequest("newName");

        UserDto dto = UserDto.builder()
                .id(5L)
                .username("newName")
                .build();

        when(userService.updateUsername(eq(5L), any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/v1/users/username/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newName"));

        verify(userService).updateUsername(eq(5L), any());
    }

    @Test
    void updateUsername_shouldUseCurrentUserId() throws Exception {
        ChangeUserUsernameRequest request =
                new ChangeUserUsernameRequest("self");

        when(securityUtil.getCurrentUserId())
                .thenReturn(7L);

        UserDto dto = UserDto.builder()
                .id(7L)
                .username("self")
                .build();

        when(userService.updateUsername(eq(7L), any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(securityUtil).getCurrentUserId();
        verify(userService).updateUsername(eq(7L), any());
    }

    @Test
    void updatePassword_shouldReturn201() throws Exception {
        ChangeUserPasswordRequest request =
                new ChangeUserPasswordRequest("old", "new");

        when(securityUtil.getCurrentUserId())
                .thenReturn(3L);

        mockMvc.perform(patch("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(userService).updatePassword(eq(3L), any());
    }

    @Test
    void updateRole_shouldReturnUpdatedUser() throws Exception {
        ChangeUserRoleRequest request =
                new ChangeUserRoleRequest(UserRole.ADMIN);

        when(securityUtil.getCurrentUserId())
                .thenReturn(9L);

        UserDto dto = UserDto.builder()
                .id(9L)
                .role(UserRole.ADMIN)
                .build();

        when(userService.updateRole(eq(9L), any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/v1/users/role/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).updateRole(eq(9L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/8"))
                .andExpect(status().isNoContent());

        verify(userService).delete(8L);
    }
}
