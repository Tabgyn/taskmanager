package com.example.taskmanager.controller;

import com.example.taskmanager.domain.*;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private User user;
    private Task task;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        user = userRepository.save(User.builder()
                .name("Test User")
                .email("taskuser@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.USER)
                .build());

        task = taskRepository.save(Task.builder()
                .title("Existing Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .owner(user)
                .build());

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest("taskuser@example.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("POST /api/v1/users/{userId}/tasks - should create task")
    void shouldCreateTask() throws Exception {
        TaskRequest request = new TaskRequest("New Task", "Description", null, null, null);

        mockMvc.perform(post("/api/v1/users/" + user.getId() + "/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    @DisplayName("POST /api/v1/users/{userId}/tasks - should return 422 on blank title")
    void shouldReturn422OnBlankTitle() throws Exception {
        TaskRequest request = new TaskRequest("", null, null, null, null);

        mockMvc.perform(post("/api/v1/users/" + user.getId() + "/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/tasks - should return paged tasks")
    void shouldReturnPagedTasks() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/tasks")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Existing Task"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/tasks/{taskId} - should return task")
    void shouldGetTaskById() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/tasks/{taskId} - should return 404 when not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/tasks/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{userId}/tasks/{taskId} - should update task")
    void shouldUpdateTask() throws Exception {
        TaskRequest request = new TaskRequest("Updated Task", null, TaskStatus.IN_PROGRESS, null, null);

        mockMvc.perform(put("/api/v1/users/" + user.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{userId}/tasks/{taskId} - should delete and return 204")
    void shouldDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + user.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET tasks - should return 401 when no token")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/tasks"))
                .andExpect(status().isUnauthorized());
    }
}