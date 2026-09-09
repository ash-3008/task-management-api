package com.example.task_management_api.dto;
import jakarta.validation.constraints.*;
public final class AuthDtos {
    private AuthDtos() { }
    public record SignupRequest(@NotBlank @Size(max=50) String username, @NotBlank @Email String email, @NotBlank @Size(min=8, max=100) String password) { }
    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }
    public record UserUpdateRequest(@Email String email, @Size(min=8, max=100) String password) { }
    public record AuthResponse(String token, UserResponse user) { }
}