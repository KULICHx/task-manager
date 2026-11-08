package org.example.taskmanager;

import tools.jackson.databind.json.JsonMapper;
import org.example.taskmanager.dto.category.CategoryRequest;
import org.example.taskmanager.dto.task.TaskRequest;
import org.example.taskmanager.dto.user.UserRequest;
import org.example.taskmanager.entity.TaskType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class TaskControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("task_manager_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.cache.redis.enabled", () -> "false");
        registry.add("app.kafka.events.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void shouldCreateUserCategoryAndTask_ThenFetchTaskBack() throws Exception {
        var userRequest = new UserRequest("Integration User", "integration@test.com");
        var userResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long userId = objectMapper.readTree(userResult.getResponse().getContentAsString())
                .get("id").asLong();

        var categoryRequest = new CategoryRequest("Integration Category");
        var categoryResult = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper.readTree(categoryResult.getResponse().getContentAsString())
                .get("id").asLong();

        var taskRequest = new TaskRequest(
                "Integration task",
                "created via integration test",
                false,
                userId,
                categoryId,
                TaskType.WORK
        );

        var taskResult = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration task"))
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration task"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldReturnNotFound_WhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturnBadRequest_WhenTaskRequestIsInvalid() throws Exception {
        var invalidRequest = new TaskRequest(
                "",
                "desc",
                false,
                1L,
                1L,
                TaskType.WORK
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
