package com.example.task_management_api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.task_management_api.repository.ProjectRepository;
import com.example.task_management_api.repository.TaskRepository;
import com.example.task_management_api.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OwnershipSecurityIntegrationTest {

    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProjectRepository projects;

    @Autowired
    private TaskRepository tasks;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String aliceToken;
    private String bobToken;
    private Long aliceProjectId;
    private Long aliceTaskId;

    @BeforeEach
    void registerUsersAndCreateAliceResources() throws Exception {
        tasks.deleteAll();
        projects.deleteAll();
        users.deleteAll();

        aliceToken = signupAndGetToken("alice", "alice@example.com");
        bobToken = signupAndGetToken("bob", "bob@example.com");
        aliceProjectId = createProject();
        aliceTaskId = createTask();
    }

    @Test
    void aliceCanGetHerOwnProject() throws Exception {
        mockMvc.perform(get("/api/projects/" + aliceProjectId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());
    }

    @Test
    void aliceCanUpdateHerOwnProject() throws Exception {
        mockMvc.perform(put("/api/projects/" + aliceProjectId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Alice updated project", "description", "Updated"))))
                .andExpect(status().isOk());
    }

    @Test
    void aliceCanDeleteHerOwnProject() throws Exception {
        mockMvc.perform(delete("/api/projects/" + aliceProjectId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void aliceCanGetUpdateAndDeleteHerOwnTask() throws Exception {
        mockMvc.perform(get("/api/tasks/" + aliceTaskId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/tasks/" + aliceTaskId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Alice updated task", "priority", "HIGH"))))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/tasks/" + aliceTaskId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void bobCannotGetAliceProject() throws Exception {
        mockMvc.perform(get("/api/projects/" + aliceProjectId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void bobCannotUpdateAliceProject() throws Exception {
        mockMvc.perform(put("/api/projects/" + aliceProjectId).header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Bob hijack"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void bobCannotDeleteAliceProject() throws Exception {
        mockMvc.perform(delete("/api/projects/" + aliceProjectId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void bobCannotGetUpdateOrDeleteAliceTask() throws Exception {
        mockMvc.perform(get("/api/tasks/" + aliceTaskId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/tasks/" + aliceTaskId).header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Bob hijack"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/tasks/" + aliceTaskId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestsCannotAccessAliceResources() throws Exception {
        mockMvc.perform(get("/api/projects/" + aliceProjectId)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/tasks/" + aliceTaskId)).andExpect(status().isUnauthorized());
    }

    @Test
    void malformedJwtCannotAccessAliceResources() throws Exception {
        mockMvc.perform(get("/api/projects/" + aliceProjectId).header("Authorization", "Bearer malformed.jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/tasks/" + aliceTaskId).header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    private String signupAndGetToken(String username, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "email", email, "password", PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(mvcResult -> {
                    JsonNode response = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
                    assertFalse(response.get("token").asText().isBlank());
                })
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Long createProject() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Alice project", "description", "Alice-owned project"))))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createTask() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks/projects/" + aliceProjectId)
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Alice task", "description", "Alice-owned task",
                                "priority", "MEDIUM", "dueDate", "2030-01-01"))))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Map<String, String> values) throws Exception {
        return objectMapper.writeValueAsString(values);
    }
}
