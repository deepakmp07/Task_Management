package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
    }

    @Test
    void save_Success() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals(testUser.getName(), savedUser.getName());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
    }

    @Test
    void findById_Success() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("John Doe", foundUser.get().getName());
    }

    @Test
    void findById_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findById(999L);

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByEmail_Success() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("john.doe@example.com", foundUser.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void existsByEmail_ReturnsTrue_WhenEmailExists() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_ReturnsFalse_WhenEmailDoesNotExist() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findAll_Success() {
        // Arrange
        User user1 = new User(null, "User 1", "user1@example.com");
        User user2 = new User(null, "User 2", "user2@example.com");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // Act
        var users = userRepository.findAll();

        // Assert
        assertEquals(2, users.size());
    }

    @Test
    void delete_Success() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);
        entityManager.flush();

        // Assert
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }
}