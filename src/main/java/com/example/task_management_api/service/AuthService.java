package com.example.task_management_api.service;
import com.example.task_management_api.dto.AuthDtos.*;
import com.example.task_management_api.dto.UserResponse;
import com.example.task_management_api.model.User;
import com.example.task_management_api.repository.UserRepository;
import com.example.task_management_api.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager authenticationManager; private final JwtService jwt;
    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwt) { this.users=users; this.encoder=encoder; this.authenticationManager=authenticationManager; this.jwt=jwt; }
    public AuthResponse signup(SignupRequest request) { if (users.existsByUsername(request.username()) || users.existsByEmail(request.email())) throw new IllegalArgumentException("Username or email already exists"); User user = users.save(new User(request.username(), request.email(), encoder.encode(request.password()))); return new AuthResponse(jwt.create(user), UserResponse.from(user)); }
    public AuthResponse login(LoginRequest request) { authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password())); User user = users.findByUsername(request.username()).orElseThrow(); return new AuthResponse(jwt.create(user), UserResponse.from(user)); }
}