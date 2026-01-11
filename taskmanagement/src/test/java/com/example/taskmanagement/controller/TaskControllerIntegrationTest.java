package com.example.taskmanagement.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.taskmanagement.dto.TaskDTO;
import com.example.taskmanagement.dto.TaskStatusUpdateDTO;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.enums.TaskPriority;
import com.example.taskmanagement.enums.TaskStatus;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void createTask_Success() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setStatus(TaskStatus.TODO);
        taskDTO.setPriority(TaskPriority.HIGH);
        taskDTO.setDueDate(LocalDate.now().plusDays(7));
        taskDTO.setAssignedToId(testUser.getId());

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-KEY", "test-api-key")
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assignedToId").value(testUser.getId()))
                .andExpect(jsonPath("$.assignedToName").value("John Doe"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void createTask_WithoutAssignedUser_Success() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Unassigned Task");
        taskDTO.setDescription("No assignee");
        taskDTO.setStatus(TaskStatus.TODO);
        taskDTO.setPriority(TaskPriority.LOW);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignedToId").doesNotExist())
                .andExpect(jsonPath("$.assignedToName").doesNotExist());
    }

    @Test
    void createTask_ValidationError_TitleTooShort() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("AB");
        taskDTO.setStatus(TaskStatus.TODO);
        taskDTO.setPriority(TaskPriority.LOW);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    void createTask_UserNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        taskDTO.setStatus(TaskStatus.TODO);
        taskDTO.setPriority(TaskPriority.HIGH);
        taskDTO.setAssignedToId(999L);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void getAllTasks_Success() throws Exception {
        // Arrange - Create test tasks
        Task task1 = createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        Task task2 = createTask("Task 2", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM);
        taskRepository.save(task1);
        taskRepository.save(task2);

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // Required for your Security Filter
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAllTasks_WithStatusFilter_Success() throws Exception {
        // Arrange
        Task todoTask = createTask("TODO Task", TaskStatus.TODO, TaskPriority.HIGH);
        Task inProgressTask = createTask("In Progress Task", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM);
        taskRepository.save(todoTask);
        taskRepository.save(inProgressTask);

        // Act & Assert - Filter by TODO status
        mockMvc.perform(get("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("TODO"));
    }

    @Test
    void getAllTasks_WithPriorityFilter_Success() throws Exception {
        // Arrange
        Task highTask = createTask("High Priority", TaskStatus.TODO, TaskPriority.HIGH);
        Task lowTask = createTask("Low Priority", TaskStatus.TODO, TaskPriority.LOW);
        taskRepository.save(highTask);
        taskRepository.save(lowTask);

        // Act & Assert - Filter by HIGH priority
        mockMvc.perform(get("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"));
    }

    @Test
    void getAllTasks_WithMultipleFilters_Success() throws Exception {
        // Arrange
        Task matchingTask = createTask("Matching", TaskStatus.TODO, TaskPriority.HIGH);
        Task nonMatchingTask = createTask("Non-matching", TaskStatus.DONE, TaskPriority.LOW);
        taskRepository.save(matchingTask);
        taskRepository.save(nonMatchingTask);

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .param("status", "TODO")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Matching"));
    }

    @Test
    void getTaskById_Success() throws Exception {
        // Arrange
        Task task = createTask("Test Task", TaskStatus.TODO, TaskPriority.HIGH);
        Task savedTask = taskRepository.save(task);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{id}", savedTask.getId()).header("X-API-KEY", "test-api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void getTaskById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks/{id}", 999L)
                        .header("X-API-KEY", "test-api-key")) // Required for your Security Filter)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    @Test
    void updateTask_Success() throws Exception {
        // Arrange - Create task
        Task task = createTask("Original Title", TaskStatus.TODO, TaskPriority.LOW);
        Task savedTask = taskRepository.save(task);

        // Update DTO
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Description");
        updateDTO.setStatus(TaskStatus.IN_PROGRESS);
        updateDTO.setPriority(TaskPriority.HIGH);
        updateDTO.setDueDate(LocalDate.now().plusDays(10));
        updateDTO.setAssignedToId(testUser.getId());

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void updateTaskStatus_Success() throws Exception {
        // Arrange
        Task task = createTask("Task", TaskStatus.TODO, TaskPriority.HIGH);
        Task savedTask = taskRepository.save(task);

        TaskStatusUpdateDTO statusDTO = new TaskStatusUpdateDTO(TaskStatus.DONE);

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/{id}/status", savedTask.getId())
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.title").value("Task")); // Title unchanged
    }

    @Test
    void deleteTask_Success() throws Exception {
        // Arrange
        Task task = createTask("Task to delete", TaskStatus.TODO, TaskPriority.LOW);
        Task savedTask = taskRepository.save(task);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{id}", savedTask.getId()).header("X-API-KEY", "test-api-key"))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/api/tasks/{id}", savedTask.getId()).header("X-API-KEY", "test-api-key"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{id}", 999L).header("X-API-KEY", "test-api-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    // Helper method
    private Task createTask(String title, TaskStatus status, TaskPriority priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description");
        task.setStatus(status);
        task.setPriority(priority);
        task.setAssignedTo(testUser);
        return task;
    }
}