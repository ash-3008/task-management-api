package com.example.task_management_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.task_management_api.model.Project;
import com.example.task_management_api.model.User;
import com.example.task_management_api.repository.ProjectRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projects;

    @Mock
    private UserService users;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void owned_shouldAllowOwner_andRejectOtherUsers() {
        User alice = new User("alice", "alice@example.com", "encoded-password");
        Project aliceProject = new Project("Alice project", "Owned by Alice", alice);

        when(projects.findById(42L)).thenReturn(Optional.of(aliceProject));

        assertSame(aliceProject, projectService.owned(42L, "alice"));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> projectService.owned(42L, "bob")
        );

        assertEquals("Project is not owned by the current user", ex.getMessage());
        verify(projects, times(2)).findById(42L);
    }
}
