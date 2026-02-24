package com.example.taskmanager.service;

import com.example.taskmanager.domain.User;
import com.example.taskmanager.dto.UserRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.exception.EmailAlreadyExistsException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").name("Test").password("hashed").build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(user);

        UserRequest request = new UserRequest("Test", "test@example.com", "password123");
        UserResponse response = userService.create(request);

        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw when email already exists")
    void shouldThrowOnDuplicateEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        UserRequest request = new UserRequest("Test", "test@example.com", "password123");
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should find user by id")
    void shouldFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}