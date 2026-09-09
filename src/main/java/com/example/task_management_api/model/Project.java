package com.example.task_management_api.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects", indexes = @Index(name = "idx_projects_user_id", columnList = "user_id"))
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String name;
    @Column(length = 2000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProjectStatus status = ProjectStatus.ACTIVE;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true) private List<Task> tasks = new ArrayList<>();
    protected Project() { }
    public Project(String name, String description, User user) { this.name = name; this.description = description; this.user = user; }
    @PrePersist void setCreatedAt() { if (createdAt == null) createdAt = Instant.now(); }
    public Long getId() { return id; } public String getName() { return name; } public String getDescription() { return description; }
    public ProjectStatus getStatus() { return status; } public Instant getCreatedAt() { return createdAt; } public User getUser() { return user; }
    public void update(String name, String description, ProjectStatus status) { this.name = name; this.description = description; if (status != null) this.status = status; }
}