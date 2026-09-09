package com.example.task_management_api.dto;
import com.example.task_management_api.model.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
public final class ProjectDtos {
    private ProjectDtos() { }
    public record ProjectRequest(@NotBlank @Size(max=150) String name, @Size(max=2000) String description, ProjectStatus status) { }
    public record ProjectResponse(Long id, String name, String description, ProjectStatus status, Instant createdAt, Long userId) { public static ProjectResponse from(Project p) { return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getStatus(), p.getCreatedAt(), p.getUser().getId()); } }
}