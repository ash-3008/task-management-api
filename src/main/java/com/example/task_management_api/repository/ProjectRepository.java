package com.example.task_management_api.repository;
import com.example.task_management_api.model.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectRepository extends JpaRepository<Project, Long> { Optional<Project> findByIdAndUserId(Long id, Long userId); java.util.List<Project> findAllByUserId(Long userId); }