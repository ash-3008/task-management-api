package com.example.task_management_api.service;
import com.example.task_management_api.model.User;
import com.example.task_management_api.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public UserService(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }
    public User find(Long id) { return users.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found")); }
    public User findByUsername(String username) { return users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found")); }
    public List<User> list() { return users.findAll(); }
    public void delete(Long id) { users.delete(find(id)); }
    public User updateAccount(String username, String email, String password) {
        User user = findByUsername(username);
        if (email != null && !email.equals(user.getEmail()) && users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.updateAccount(email, password == null ? null : encoder.encode(password));
        return users.save(user);
    }
    @Override public UserDetails loadUserByUsername(String username) { User user = findByUsername(username); return org.springframework.security.core.userdetails.User.withUsername(user.getUsername()).password(user.getPasswordHash()).roles(user.getRole().name()).build(); }
}