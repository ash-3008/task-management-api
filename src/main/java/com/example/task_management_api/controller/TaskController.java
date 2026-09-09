package com.example.task_management_api.controller;

import com.example.task_management_api.dto.TaskDtos.TaskRequest;
import com.example.task_management_api.dto.TaskDtos.TaskResponse;
import com.example.task_management_api.model.TaskPriority;
import com.example.task_management_api.model.TaskStatus;
import com.example.task_management_api.service.TaskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService tasks;

    public TaskController(TaskService tasks) {
        this.tasks = tasks;
    }

    @GetMapping
    public Page<TaskResponse> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) LocalDate dueDate,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
        }
        String sortProperty = switch (sort) {
            case "dueDate", "createdAt", "updatedAt", "title", "priority", "status" -> sort;
            default -> throw new IllegalArgumentException("Unsupported sort field: " + sort);
        };
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException("direction must be asc or desc"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));
        return tasks.list(authentication.getName(), projectId, status, priority, dueDate, dueDateFrom, dueDateTo, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id, Authentication authentication) {
        return tasks.get(id, authentication.getName());
    }

    @PostMapping("/projects/{projectId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request,
                               Authentication authentication) {
        return tasks.create(projectId, request, authentication.getName());
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request,
                               Authentication authentication) {
        return tasks.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        tasks.delete(id, authentication.getName());
    }
}
