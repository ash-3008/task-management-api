package com.example.task_management_api.controller;

import com.example.task_management_api.dto.UserResponse;
import com.example.task_management_api.dto.AuthDtos.UserUpdateRequest;
import jakarta.validation.Valid;
import com.example.task_management_api.service.UserService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return UserResponse.from(users.findByUsername(authentication.getName()));
    }

    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UserUpdateRequest request, Authentication authentication) {
        return UserResponse.from(users.updateAccount(authentication.getName(), request.email(), request.password()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return users.list().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(users.find(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        users.delete(id);
    }
}
