package com.example.task_management_api.controller;

import com.example.task_management_api.dto.ProjectDtos.ProjectRequest;
import com.example.task_management_api.dto.ProjectDtos.ProjectResponse;
import com.example.task_management_api.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projects;

    public ProjectController(ProjectService projects) {
        this.projects = projects;
    }

    @GetMapping
    public List<ProjectResponse> list(Authentication authentication) {
        return projects.list(authentication.getName());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id, Authentication authentication) {
        return projects.get(id, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request, Authentication authentication) {
        return projects.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request,
                                  Authentication authentication) {
        return projects.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        projects.delete(id, authentication.getName());
    }
}
