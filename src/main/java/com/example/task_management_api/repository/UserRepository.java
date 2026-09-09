package com.example.task_management_api.repository;
import com.example.task_management_api.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> { Optional<User> findByUsername(String username); boolean existsByUsername(String username); boolean existsByEmail(String email); }