package com.example.taskmanager.controller;

import com.example.taskmanager.domain.TaskPriority;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.dto.PageResponse;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({GlobalExceptionHandler.class, JacksonAutoConfiguration.class})
@WithMockUser
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private final TaskResponse sampleTask = new TaskResponse(
            1L, "Test Task", "Description", TaskStatus.TODO,
            TaskPriority.MEDIUM, null, LocalDateTime.now(), LocalDateTime.now(), 1L);

    @Test
    @DisplayName("POST /api/v1/users/{userId}/tasks - should create task and return 201")
    void shouldCreateTask() throws Exception {
        when(taskService.create(any(), eq(1L))).thenReturn(sampleTask);

        TaskRequest request = new TaskRequest("Test Task", "Description", null, null, null);

        mockMvc.perform(post("/api/v1/users/1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @DisplayName("POST /api/v1/users/{userId}/tasks - should return 422 on blank title")
    void shouldReturn422OnBlankTitle() throws Exception {
        TaskRequest request = new TaskRequest("", null, null, null, null);

        mockMvc.perform(post("/api/v1/users/1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/tasks - should return paged tasks")
    void shouldReturnPagedTasks() throws Exception {
        PageResponse<TaskResponse> page = new PageResponse<>(List.of(sampleTask), 0, 10, 1, 1, true);
        when(taskService.findByOwner(eq(1L), isNull(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Task"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/tasks/{taskId} - should return task")
    void shouldGetTaskById() throws Exception {
        when(taskService.findById(1L, 1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/api/v1/users/1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/tasks/{taskId} - should return 404 when not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.findById(99L, 1L)).thenThrow(new ResourceNotFoundException("Task", 99L));

        mockMvc.perform(get("/api/v1/users/1/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{userId}/tasks/{taskId} - should update task")
    void shouldUpdateTask() throws Exception {
        when(taskService.update(eq(1L), any(), eq(1L))).thenReturn(sampleTask);

        TaskRequest request = new TaskRequest("Updated Task", null, TaskStatus.IN_PROGRESS, null, null);

        mockMvc.perform(put("/api/v1/users/1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{userId}/tasks/{taskId} - should delete and return 204")
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/users/1/tasks/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}