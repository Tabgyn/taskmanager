package com.example.taskmanager.service;

import com.example.taskmanager.domain.UserRole;
import com.example.taskmanager.dto.*;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    private User user;
    private UserDetails userDetails;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("test@example.com")
                .name("Test").password("hashed")
                .role(UserRole.USER).build();

        userDetails = new org.springframework.security.core.userdetails.User(
                "test@example.com", "hashed", List.of());

        userResponse = new UserResponse(1L, "Test", "test@example.com",
                UserRole.USER, LocalDateTime.now());
    }

    @Test
    @DisplayName("Should login and return token")
    void shouldLogin() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, null));
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(new LoginRequest("test@example.com", "password"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should register and return token")
    void shouldRegister() {
        when(userService.create(any())).thenReturn(userResponse);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        UserRequest request = new UserRequest("Test", "test@example.com", "password123");
        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }
}