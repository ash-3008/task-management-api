package com.example.task_management_api.repository;
import com.example.task_management_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> { java.util.Optional<Task> findByIdAndProjectUserId(Long id, Long userId); }