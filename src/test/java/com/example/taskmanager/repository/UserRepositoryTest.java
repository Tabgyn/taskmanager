package com.example.taskmanager.repository;

import com.example.taskmanager.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindByEmail() {
        userRepository.save(User.builder()
                .email("user@example.com")
                .name("User")
                .password("hashed")
                .build());

        assertThat(userRepository.findByEmail("user@example.com")).isPresent();
    }

    @Test
    @DisplayName("Should return empty for unknown email")
    void shouldReturnEmptyForUnknownEmail() {
        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    @DisplayName("Should check email existence")
    void shouldCheckEmailExists() {
        userRepository.save(User.builder()
                .email("exists@example.com")
                .name("User")
                .password("hashed")
                .build());

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nope@example.com")).isFalse();
    }
}