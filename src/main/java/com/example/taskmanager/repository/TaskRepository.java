package com.example.taskmanager.repository;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Task> findByOwnerIdAndStatus(Long ownerId, TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.owner.id = :ownerId AND " +
            "(:status IS NULL OR t.status = :status)")
    Page<Task> findByOwnerIdWithFilters(
            @Param("ownerId") Long ownerId,
            @Param("status") TaskStatus status,
            Pageable pageable
    );

    long countByOwnerIdAndStatus(Long ownerId, TaskStatus status);
}