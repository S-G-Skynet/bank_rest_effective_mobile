package com.example.bankcards.service;

import com.example.bankcards.config.mapper.UserMapper;
import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.user.UserChangePasswordException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.exception.user.UserWithThisUsernameAlreadyExist;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.PasswordConfig;
import com.example.bankcards.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordConfig passwordConfig;

    @InjectMocks
    private UserServiceImpl userService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void create_shouldSaveUser_withEncryptedPassword() {
        CreateUserRequest request = new CreateUserRequest("john", "12345");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(passwordConfig.passwordEncoder()).thenReturn(passwordEncoder);
        User savedUser = User.builder()
                .id(1L)
                .username("john")
                .passwordHash("12345")
                .role(UserRole.USER)
                .build();

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(any(User.class)))
                .thenReturn(new UserDto(1L, "john", UserRole.USER));

        UserDto result = userService.create(request, UserRole.USER);
        verify(userRepository).save(captor.capture());
        User userToSave = captor.getValue();

        assertEquals("john", userToSave.getUsername());
        assertEquals(UserRole.USER, userToSave.getRole());

        assertNotEquals("12345", userToSave.getPasswordHash());
        assertTrue(passwordEncoder.matches("12345", userToSave.getPasswordHash()));

        assertEquals("john", result.getUsername());
    }

    @Test
    void create_shouldThrowException_whenUsernameAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("john", "12345");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(
                UserWithThisUsernameAlreadyExist.class,
                () -> userService.create(request, UserRole.USER)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnUser_whenExists() {
        User user = User.builder()
                .id(1L)
                .username("john")
                .passwordHash("password")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user))
                .thenReturn(new UserDto(1L, "john", UserRole.ADMIN));

        UserDto dto = userService.getById(1L);

        assertEquals(1L, dto.getId());
        assertEquals("john", dto.getUsername());
        assertEquals(UserRole.ADMIN, dto.getRole());
    }

    @Test
    void getById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(1L)
        );
    }

    @Test
    void getAll_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 2);

        User user1 = User.builder()
                .id(1L)
                .username("u1")
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        User user2 = User.builder()
                .id(2L)
                .username("u2")
                .passwordHash("password")
                .role(UserRole.ADMIN)
                .build();

        Page<User> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toDto(user1)).thenReturn(new UserDto(1L, "u1", UserRole.USER));
        when(userMapper.toDto(user2)).thenReturn(new UserDto(2L, "u2", UserRole.ADMIN));

        Page<UserDto> result = userService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ChangeUserUsernameRequest request = new ChangeUserUsernameRequest("new");

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUsername(1L, request)
        );
    }

    @Test
    void updateUser_shouldThrowException_whenUsernameIsAlreadyExists() {
        User user = User.builder()
                .id(1L)
                .username("John")
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("John")).thenReturn(true);

        ChangeUserUsernameRequest requestUsername =
                new ChangeUserUsernameRequest("John");

        assertThrows(
                UserWithThisUsernameAlreadyExist.class,
                () -> userService.updateUsername(1L, requestUsername)
        );
    }

    @Test
    void changeUsername_shouldUpdateUsernameUser_whenSuccess() {
        User user = User.builder()
                .id(1L)
                .username("old")
                .passwordHash("password")
                .role(UserRole.USER)
                .build();

        ChangeUserUsernameRequest request = new ChangeUserUsernameRequest("new");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(new UserDto(1L, "new", user.getRole()));

        UserDto dto = userService.updateUsername(1L, request);

        assertEquals("new", dto.getUsername());
        assertEquals(UserRole.USER, dto.getRole());
    }

    @Test
    void changeRole_shouldUpdateRoleUser_whenSuccess() {
        User user = User.builder()
                .id(1L)
                .username("John")
                .passwordHash("password")
                .role(UserRole.USER)
                .build();

        ChangeUserRoleRequest request = new ChangeUserRoleRequest(UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User savedUser = User.builder()
                .id(1L)
                .username("John")
                .passwordHash("password")
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser))
                .thenReturn(new UserDto(1L, "John", savedUser.getRole()));

        UserDto dto = userService.updateRole(1L, request);

        assertEquals("John", dto.getUsername());
        assertEquals(UserRole.ADMIN, dto.getRole());
    }

    @Test
    void changePassword_shouldUpdatePasswordUser_whenSuccess() {
        //arrange
        when(passwordConfig.passwordEncoder()).thenReturn(passwordEncoder);
        String encodedOldPassword = passwordEncoder.encode("old");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        User user = User.builder()
                .id(1L)
                .username("John")
                .passwordHash(encodedOldPassword)
                .role(UserRole.USER)
                .build();
        ChangeUserPasswordRequest request =
                new ChangeUserPasswordRequest("old", "new");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        //act
        userService.updatePassword(1L, request);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        //assert
        assertTrue(passwordEncoder.matches("new", savedUser.getPasswordHash()));
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void changePassword_shouldThrowException_whenPasswordHashIsNotMatch() {

        when(passwordConfig.passwordEncoder()).thenReturn(passwordEncoder);

        String encodedOldPassword = passwordEncoder.encode("old");

        User user = User.builder()
                .id(1L)
                .username("john")
                .passwordHash(encodedOldPassword)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ChangeUserPasswordRequest request =
                new ChangeUserPasswordRequest("notOld", "new");

        assertThrows(
                UserChangePasswordException.class,
                () -> userService.updatePassword(1L, request)
        );

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void changePassword_shouldThrowException_whenOldAndNewPasswordAreEquals() {

        when(passwordConfig.passwordEncoder()).thenReturn(passwordEncoder);

        String encodedOldPassword = passwordEncoder.encode("old");

        User user = User.builder()
                .id(1L)
                .username("john")
                .passwordHash(encodedOldPassword)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ChangeUserPasswordRequest request =
                new ChangeUserPasswordRequest("old", "old");

        assertThrows(
                UserChangePasswordException.class,
                () -> userService.updatePassword(1L, request)
        );

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void delete_shouldThrowException_whenUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> userService.delete(1L)
        );
    }

    @Test
    void delete_shouldDeleteUser_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }
}