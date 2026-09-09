package com.example.task_management_api.dto;
import com.example.task_management_api.model.User;
import java.time.Instant;
public record UserResponse(Long id, String username, String email, String role, Instant createdAt) { public static UserResponse from(User user) { return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), user.getCreatedAt()); } }