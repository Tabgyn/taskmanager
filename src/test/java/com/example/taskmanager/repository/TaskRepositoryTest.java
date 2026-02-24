package com.example.taskmanager.repository;

import com.example.taskmanager.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired TaskRepository taskRepository;
    @Autowired UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("test@example.com")
                .name("Test User")
                .password("hashed")
                .build());

        taskRepository.save(Task.builder().title("Task 1").status(TaskStatus.TODO).priority(TaskPriority.HIGH).owner(user).build());
        taskRepository.save(Task.builder().title("Task 2").status(TaskStatus.IN_PROGRESS).priority(TaskPriority.MEDIUM).owner(user).build());
        taskRepository.save(Task.builder().title("Task 3").status(TaskStatus.DONE).priority(TaskPriority.LOW).owner(user).build());
    }

    @Test
    @DisplayName("Should find all tasks by owner")
    void shouldFindTasksByOwner() {
        Page<Task> tasks = taskRepository.findByOwnerId(user.getId(), PageRequest.of(0, 10));
        assertThat(tasks.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should filter tasks by owner and status")
    void shouldFilterByStatus() {
        Page<Task> tasks = taskRepository.findByOwnerIdAndStatus(user.getId(), TaskStatus.TODO, PageRequest.of(0, 10));
        assertThat(tasks.getTotalElements()).isEqualTo(1);
        assertThat(tasks.getContent().getFirst().getTitle()).isEqualTo("Task 1");
    }

    @Test
    @DisplayName("Should count tasks by status")
    void shouldCountByStatus() {
        long count = taskRepository.countByOwnerIdAndStatus(user.getId(), TaskStatus.DONE);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return empty when owner has no tasks")
    void shouldReturnEmptyForUnknownOwner() {
        Page<Task> tasks = taskRepository.findByOwnerId(999L, PageRequest.of(0, 10));
        assertThat(tasks.getTotalElements()).isZero();
    }
}