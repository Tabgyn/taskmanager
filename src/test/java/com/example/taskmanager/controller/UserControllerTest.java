package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.domain.UserRole;
import com.example.taskmanager.exception.EmailAlreadyExistsException;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, JacksonAutoConfiguration.class})
@WithMockUser
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private final UserResponse sampleResponse = new UserResponse(
            1L, "Test User", "test@example.com", UserRole.USER, LocalDateTime.now());

    @Test
    @DisplayName("POST /api/v1/users - should create user and return 201")
    void shouldCreateUser() throws Exception {
        when(userService.create(any())).thenReturn(sampleResponse);

        UserRequest request = new UserRequest("Test User", "test@example.com", "password123");

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/users - should return 422 on invalid input")
    void shouldReturn422OnInvalidInput() throws Exception {
        UserRequest request = new UserRequest("", "not-an-email", "short");

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/users - should return 409 on duplicate email")
    void shouldReturn409OnDuplicateEmail() throws Exception {
        when(userService.create(any())).thenThrow(new EmailAlreadyExistsException("test@example.com"));

        UserRequest request = new UserRequest("Test", "test@example.com", "password123");

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - should return user")
    void shouldGetUserById() throws Exception {
        when(userService.findById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}