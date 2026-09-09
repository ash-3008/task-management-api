package com.example.task_management_api.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_email", columnList = "email", unique = true))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String username;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @Column(nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserRole role = UserRole.USER;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) private List<Project> projects = new ArrayList<>();
    protected User() { }
    public User(String username, String email, String passwordHash) { this.username = username; this.email = email; this.passwordHash = passwordHash; }
    public void updateAccount(String email, String passwordHash) { if (email != null) this.email = email; if (passwordHash != null) this.passwordHash = passwordHash; }
    @PrePersist void setCreatedAt() { if (createdAt == null) createdAt = Instant.now(); }
    public Long getId() { return id; } public String getUsername() { return username; } public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; } public UserRole getRole() { return role; } public Instant getCreatedAt() { return createdAt; }
    public void setRole(UserRole role) { this.role = role; }
}