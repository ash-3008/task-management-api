package com.example.task_management_api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.task_management_api.model.*;
import com.example.task_management_api.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskManagementApiApplicationTests {

    private static final String PASSWORD = "Password123";

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired UserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired TaskRepository tasks;
    @Autowired PasswordEncoder passwordEncoder;

    private String aliceToken;
    private String bobToken;
    private String adminToken;
    private Long aliceProjectId;
    private Long aliceTaskId;

    @BeforeEach
    void setUp() throws Exception {
        tasks.deleteAll();
        projects.deleteAll();
        users.deleteAll();
        User alice = saveUser("alice", "alice@example.com", UserRole.USER);
        User bob = saveUser("bob", "bob@example.com", UserRole.USER);
        User admin = saveUser("admin", "admin@example.com", UserRole.ADMIN);
        aliceToken = login("alice");
        bobToken = login("bob");
        adminToken = login("admin");
        aliceProjectId = projects.save(new Project("Alice project", "Owned by Alice", alice)).getId();
        aliceTaskId = tasks.save(new Task("Alice task", "Owned by Alice", TaskPriority.MEDIUM,
                LocalDate.now().plusDays(3), projects.findById(aliceProjectId).orElseThrow())).getId();
        Assertions.assertNotNull(bob);
        Assertions.assertNotNull(admin);
    }

    @Test
    void signupAndLoginWork() throws Exception {
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "charlie", "email", "charlie@example.com", "password", PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.user.username").value("charlie"))
                .andExpect(jsonPath("$.user.role").value("USER"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "alice", "password", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("alice@example.com"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "alice", "email", "another@example.com", "password", PASSWORD)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "another", "email", "alice@example.com", "password", PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidLoginAndUnauthenticatedAccessAreRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "alice", "password", "wrong-password")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/projects").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanCreateUpdateAndDeleteOwnProjectAndTask() throws Exception {
        Long projectId = readLong(mockMvc.perform(post("/api/projects").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("name", "New project", "description", "Details", "status", "ACTIVE")))
                .andExpect(status().isCreated()).andReturn(), "id");
        Long taskId = readLong(mockMvc.perform(post("/api/tasks/projects/" + projectId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("title", "New task", "description", "Details", "status", "COMPLETED", "priority", "HIGH",
                                "dueDate", "2030-01-10")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("COMPLETED")).andReturn(), "id");

        mockMvc.perform(put("/api/projects/" + projectId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("name", "Updated project", "description", "Updated", "status", "COMPLETED")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mockMvc.perform(put("/api/tasks/" + taskId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("title", "Updated task", "description", "Updated", "status", "IN_PROGRESS",
                                "priority", "LOW", "dueDate", "2030-01-11")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/projects/" + projectId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void userCannotModifyAnotherUsersProjectOrTask() throws Exception {
        mockMvc.perform(put("/api/projects/" + aliceProjectId).header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON).content(json("name", "Hijack")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/projects/" + aliceProjectId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/tasks/" + aliceTaskId).header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON).content(json("title", "Hijack")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/tasks/" + aliceTaskId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingResourceReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/projects/999999").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void adminCanManageUsersButNormalUserCannot() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
        Long bobId = users.findByUsername("bob").orElseThrow().getId();
        mockMvc.perform(get("/api/users/" + bobId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("bob"));
        mockMvc.perform(get("/api/users").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userAccountCrudAndAdminDeleteWork() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("alice"));
        mockMvc.perform(put("/api/users/me").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", "alice.updated@example.com", "password", "NewPassword123")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("alice.updated@example.com"));
        Long bobId = users.findByUsername("bob").orElseThrow().getId();
        mockMvc.perform(delete("/api/users/" + bobId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
        Assertions.assertFalse(users.findByUsername("bob").isPresent());
    }

    @Test
    void validationAndInvalidEnumsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "", "email", "not-an-email", "password", "short")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/projects").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(json("name", "")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/tasks/projects/" + aliceProjectId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(json("title", "")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/projects").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(json("name", "P", "status", "UNKNOWN")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks?status=UNKNOWN").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/projects").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(get("/api/projects/not-a-number").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void taskPaginationFilteringAndSortingWork() throws Exception {
        Long secondProjectId = readLong(mockMvc.perform(post("/api/projects").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(json("name", "Second project")))
                .andExpect(status().isCreated()).andReturn(), "id");
        createTask(aliceProjectId, "Low old", "LOW", "TODO", "2025-01-01");
        Long highId = createTask(aliceProjectId, "High current", "HIGH", "IN_PROGRESS", "2027-01-01");
        createTask(secondProjectId, "Medium future", "MEDIUM", "COMPLETED", "2028-01-01");

        mockMvc.perform(get("/api/tasks?page=0&size=2&sort=title&direction=asc")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].title").value("Alice task"));
        mockMvc.perform(get("/api/tasks?status=IN_PROGRESS").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[*].title", hasItem("High current")));
        mockMvc.perform(get("/api/tasks?priority=HIGH").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[*].title", hasItem("High current")));
        mockMvc.perform(get("/api/tasks?projectId=" + secondProjectId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/tasks?dueDateFrom=2027-01-01&dueDateTo=2027-12-31")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/tasks?sort=dueDate&direction=asc").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Low old"));
        Assertions.assertNotNull(highId);
    }

    private User saveUser(String username, String email, UserRole role) {
        User user = new User(username, email, passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        return users.save(user);
    }

    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", username, "password", PASSWORD))).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Long createTask(Long projectId, String title, String priority, String status, String dueDate) throws Exception {
        Long id = readLong(mockMvc.perform(post("/api/tasks/projects/" + projectId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("title", title, "priority", priority, "dueDate", dueDate)))
                .andExpect(status().isCreated()).andReturn(), "id");
        mockMvc.perform(put("/api/tasks/" + id).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("title", title, "status", status, "priority", priority, "dueDate", dueDate)))
                .andExpect(status().isOk());
        return id;
    }

    private Long readLong(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString()).get(field);
        return node.asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object... values) throws Exception {
        var node = objectMapper.createObjectNode();
        for (int i = 0; i < values.length; i += 2) {
            node.put(values[i].toString(), values[i + 1].toString());
        }
        return objectMapper.writeValueAsString(node);
    }
}
