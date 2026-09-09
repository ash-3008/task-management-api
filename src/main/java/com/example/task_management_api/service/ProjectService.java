package com.example.task_management_api.service;
import com.example.task_management_api.dto.ProjectDtos.*;
import com.example.task_management_api.model.*;
import com.example.task_management_api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
@Service public class ProjectService {
    private final ProjectRepository projects; private final UserService users;
    public ProjectService(ProjectRepository projects, UserService users) { this.projects=projects; this.users=users; }
    public List<ProjectResponse> list(String username) { return projects.findAllByUserId(users.findByUsername(username).getId()).stream().map(ProjectResponse::from).toList(); }
    public ProjectResponse get(Long id, String username) { return ProjectResponse.from(owned(id, username)); }
    public ProjectResponse create(ProjectRequest request, String username) { return ProjectResponse.from(projects.save(new Project(request.name(), request.description(), users.findByUsername(username)))); }
    public ProjectResponse update(Long id, ProjectRequest request, String username) { Project project=owned(id,username); project.update(request.name(),request.description(),request.status()); return ProjectResponse.from(projects.save(project)); }
    public void delete(Long id, String username) { projects.delete(owned(id, username)); }
    public Long userId(String username) { return users.findByUsername(username).getId(); }
    public Project owned(Long id, String username) {
        Project project = projects.findById(id).orElseThrow(() -> new EntityNotFoundException("Project not found"));
        if (!project.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Project is not owned by the current user");
        }
        return project;
    }
}