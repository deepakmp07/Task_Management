package com.example.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.taskmanagement.dto.UserDTO;
import com.example.taskmanagement.entity.User;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "test-api-key";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_Success() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setName("John Doe");
        userDTO.setEmail("john.doe@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createUser_ValidationError_NameTooShort() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setName("J");
        userDTO.setEmail("john.doe@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void createUser_ValidationError_InvalidEmail() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setName("John Doe");
        userDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void createUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Arrange - Create first user
        User existingUser = new User();
        existingUser.setName("Jane Doe");
        existingUser.setEmail("john.doe@example.com");
        userRepository.save(existingUser);

        // Try to create user with same email
        UserDTO userDTO = new UserDTO();
        userDTO.setName("John Doe");
        userDTO.setEmail("john.doe@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("Email already exists")));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        // Arrange - Create test users
        User user1 = new User(null, "John Doe", "john@example.com");
        User user2 = new User(null, "Jane Smith", "jane@example.com");
        userRepository.save(user1);
        userRepository.save(user2);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.content[1].name").exists());
    }

    @Test
    void getAllUsers_WithPagination_Success() throws Exception {
        // Arrange - Create 5 users
        for (int i = 1; i <= 5; i++) {
            User user = new User(null, "User " + i, "user" + i + "@example.com");
            userRepository.save(user);
        }

        // Act & Assert - Request page 0, size 2
        mockMvc.perform(get("/api/users")
                        .header("X-API-KEY", "test-api-key") // <--- ADD THIS LINE
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void getUserById_Success() throws Exception {
        // Arrange
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = userRepository.save(user);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()).header("X-API-KEY", "test-api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", 999L)
                        .header("X-API-KEY", "test-api-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }
}
