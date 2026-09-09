package com.example.task_management_api.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "tasks", indexes = { @Index(name = "idx_tasks_project_id", columnList = "project_id"), @Index(name = "idx_tasks_status_priority", columnList = "status,priority") })
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 4000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TaskStatus status = TaskStatus.TODO;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TaskPriority priority = TaskPriority.MEDIUM;
    private LocalDate dueDate;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false) private Project project;
    protected Task() { }
    public Task(String title, String description, TaskPriority priority, LocalDate dueDate, Project project) { this(title, description, null, priority, dueDate, project); }
    public Task(String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate, Project project) { this.title = title; this.description = description; if (status != null) this.status = status; if (priority != null) this.priority = priority; this.dueDate = dueDate; this.project = project; }
    @PrePersist void created() { createdAt = Instant.now(); updatedAt = createdAt; } @PreUpdate void updated() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public String getTitle() { return title; } public String getDescription() { return description; } public TaskStatus getStatus() { return status; }
    public TaskPriority getPriority() { return priority; } public LocalDate getDueDate() { return dueDate; } public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; } public Project getProject() { return project; }
    public void update(String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate) { this.title = title; this.description = description; if (status != null) this.status = status; if (priority != null) this.priority = priority; this.dueDate = dueDate; }
}