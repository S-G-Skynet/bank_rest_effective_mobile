package com.example.bankcards.controller;

import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @PostMapping("/create/client")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto createClient(@RequestBody CreateUserRequest request) {
        return userService.create(request, UserRole.USER);
    }

    @PostMapping("/create/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto createAdmin(@RequestBody CreateUserRequest request) {
        return userService.create(request, UserRole.ADMIN);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserDto getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> getAll(Pageable pageable) {
        return userService.getAll(pageable);
    }

    @PutMapping("/username/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateUsernameByAdmin(
            @PathVariable Long id,
            @RequestBody ChangeUserUsernameRequest request
    ) {
        return userService.updateUsername(id, request);
    }

    @PutMapping
    public UserDto updateUsername(@RequestBody ChangeUserUsernameRequest request) {
        Long id = securityUtil.getCurrentUserId();
        return userService.updateUsername(id, request);
    }

    @PatchMapping
    public ResponseEntity<Void> updatePassword(@RequestBody ChangeUserPasswordRequest request) {
        Long id = securityUtil.getCurrentUserId();
        userService.updatePassword(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRole(@RequestBody ChangeUserRoleRequest request, @PathVariable Long id) {
        return userService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

