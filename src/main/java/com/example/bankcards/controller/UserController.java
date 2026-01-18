package com.example.bankcards.controller;

import com.example.bankcards.config.swagger.errors.CommonApiErrors;
import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.UserService;
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

@Tag(
        name = "Users",
        description = "Управление пользователями: создание, получение, обновление данных, смена ролей и удаление"
)
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @Operation(
            summary = "Создать клиента",
            description = "Создание пользователя с ролью USER. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
    })
    @CommonApiErrors(badRequest = true, forbidden = true, cardConflict = true)
    @PostMapping("/create/client")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createClient(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(request, UserRole.USER));
    }

    @Operation(
            summary = "Создать администратора",
            description = "Создание пользователя с ролью ADMIN. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Администратор успешно создан"),
    })
    @CommonApiErrors(forbidden = true, userConflict = true)
    @PostMapping("/create/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createAdmin(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(request, UserRole.ADMIN));
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Администратор может получить любого пользователя, обычный пользователь — только себя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
    })
    @CommonApiErrors(forbidden = true, userNotFound = true)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserDto getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(
            summary = "Получить список пользователей",
            description = "Постраничное получение всех пользователей. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список пользователей"),
    })
    @CommonApiErrors(forbidden = true)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> getAll(@ParameterObject
                                @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
                                Pageable pageable) {
        return userService.getAll(pageable);
    }

    @Operation(
            summary = "Изменить username пользователя (администратор)",
            description = "Администратор может изменить username любого пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username успешно изменен"),
    })
    @CommonApiErrors(forbidden = true, userNotFound = true)
    @PutMapping("/username/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateUsernameByAdmin(
            @PathVariable Long id,
            @RequestBody ChangeUserUsernameRequest request
    ) {
        return userService.updateUsername(id, request);
    }

    @Operation(
            summary = "Изменить свой username",
            description = "Пользователь изменяет username своей учетной записи"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username успешно изменен"),
    })
    @CommonApiErrors(userNotFound = true)
    @PutMapping
    public UserDto updateUsername(@RequestBody ChangeUserUsernameRequest request) {
        Long id = securityUtil.getCurrentUserId();
        return userService.updateUsername(id, request);
    }

    @Operation(
            summary = "Изменить пароль",
            description = "Пользователь изменяет пароль своей учетной записи"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пароль успешно изменен")
    })
    @PatchMapping
    public ResponseEntity<Void> updatePassword(@RequestBody ChangeUserPasswordRequest request) {
        Long id = securityUtil.getCurrentUserId();
        userService.updatePassword(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Изменить роль пользователя",
            description = "Администратор изменяет роль пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль успешно изменена"),
    })
    @CommonApiErrors(forbidden = true)
    @PutMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRole(
            @PathVariable Long id,
            @RequestBody ChangeUserRoleRequest request
    ) {
        return userService.updateRole(id, request);
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаление пользователя по ID. Доступно только администратору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удален"),
    })
    @CommonApiErrors(forbidden = true, userNotFound = true)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


