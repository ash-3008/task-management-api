package com.example.task_management_api.dto;
import com.example.task_management_api.model.*;
import jakarta.validation.constraints.*;
import java.time.*;
public final class TaskDtos {
    private TaskDtos() { }
    public record TaskRequest(@NotBlank @Size(max=200) String title, @Size(max=4000) String description, TaskStatus status, TaskPriority priority, LocalDate dueDate) { }
    public record TaskResponse(Long id, String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate, Instant createdAt, Instant updatedAt, Long projectId) { public static TaskResponse from(Task t) { return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(), t.getDueDate(), t.getCreatedAt(), t.getUpdatedAt(), t.getProject().getId()); } }
}