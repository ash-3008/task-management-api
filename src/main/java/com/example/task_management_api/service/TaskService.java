package com.example.task_management_api.service;
import com.example.task_management_api.dto.TaskDtos.*;
import com.example.task_management_api.model.*;
import com.example.task_management_api.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
@Service public class TaskService {
    private final TaskRepository tasks; private final ProjectService projects;
    public TaskService(TaskRepository tasks, ProjectService projects) { this.tasks=tasks; this.projects=projects; }
    public Page<TaskResponse> list(String username, Long projectId, TaskStatus status, TaskPriority priority,
                                   LocalDate dueDate, LocalDate dueDateFrom, LocalDate dueDateTo, Pageable pageable) {
        if (dueDateFrom != null && dueDateTo != null && dueDateFrom.isAfter(dueDateTo)) {
            throw new IllegalArgumentException("dueDateFrom must not be after dueDateTo");
        }
        Long userId=projectsUserId(username);
        Specification<Task> spec=(root,query,cb)->cb.equal(root.get("project").get("user").get("id"),userId);
        if(projectId!=null) spec=spec.and((r,q,c)->c.equal(r.get("project").get("id"),projectId));
        if(status!=null) spec=spec.and((r,q,c)->c.equal(r.get("status"),status));
        if(priority!=null) spec=spec.and((r,q,c)->c.equal(r.get("priority"),priority));
        if(dueDate!=null) spec=spec.and((r,q,c)->c.equal(r.get("dueDate"),dueDate));
        if(dueDateFrom!=null) spec=spec.and((r,q,c)->c.greaterThanOrEqualTo(r.get("dueDate"),dueDateFrom));
        if(dueDateTo!=null) spec=spec.and((r,q,c)->c.lessThanOrEqualTo(r.get("dueDate"),dueDateTo));
        return tasks.findAll(spec,pageable).map(TaskResponse::from);
    }
    public TaskResponse get(Long id,String username) { return TaskResponse.from(owned(id,username)); }
    public TaskResponse create(Long projectId,TaskRequest request,String username) { Project project=projects.owned(projectId,username); return TaskResponse.from(tasks.save(new Task(request.title(),request.description(),request.status(),request.priority(),request.dueDate(),project))); }
    public TaskResponse update(Long id,TaskRequest request,String username) { Task task=owned(id,username); task.update(request.title(),request.description(),request.status(),request.priority(),request.dueDate()); return TaskResponse.from(tasks.save(task)); }
    public void delete(Long id,String username) { tasks.delete(owned(id,username)); }
    private Task owned(Long id,String username) {
        Task task = tasks.findById(id).orElseThrow(() -> new EntityNotFoundException("Task not found"));
        if (!task.getProject().getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Task is not owned by the current user");
        }
        return task;
    }
    private Long projectsUserId(String username) { return projects.userId(username); }
}