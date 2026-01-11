package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.enums.TaskPriority;
import com.example.taskmanagement.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser = entityManager.persistAndFlush(testUser);

        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setDueDate(LocalDate.now().plusDays(7));
        testTask.setAssignedTo(testUser);
    }

    @Test
    void save_Success() {
        // Act
        Task savedTask = taskRepository.save(testTask);

        // Assert
        assertNotNull(savedTask.getId());
        assertEquals(testTask.getTitle(), savedTask.getTitle());
        assertEquals(testTask.getStatus(), savedTask.getStatus());
        assertNotNull(savedTask.getCreatedAt());
        assertNotNull(savedTask.getUpdatedAt());
    }

    @Test
    void findById_Success() {
        // Arrange
        Task savedTask = entityManager.persistAndFlush(testTask);

        // Act
        var foundTask = taskRepository.findById(savedTask.getId());

        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals("Test Task", foundTask.get().getTitle());
    }

    @Test
    void findByFilters_NoFilters_ReturnsAll() {
        // Arrange
        Task task1 = createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        Task task2 = createTask("Task 2", TaskStatus.DONE, TaskPriority.LOW);
        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Task> result = taskRepository.findByFilters(null, null, null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByFilters_FilterByStatus_Success() {
        // Arrange
        Task todoTask = createTask("TODO Task", TaskStatus.TODO, TaskPriority.HIGH);
        Task doneTask = createTask("DONE Task", TaskStatus.DONE, TaskPriority.HIGH);
        entityManager.persist(todoTask);
        entityManager.persist(doneTask);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Task> result = taskRepository.findByFilters(TaskStatus.TODO, null, null, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("TODO Task", result.getContent().get(0).getTitle());
        assertEquals(TaskStatus.TODO, result.getContent().get(0).getStatus());
    }

    @Test
    void findByFilters_FilterByPriority_Success() {
        // Arrange
        Task highTask = createTask("High Priority", TaskStatus.TODO, TaskPriority.HIGH);
        Task lowTask = createTask("Low Priority", TaskStatus.TODO, TaskPriority.LOW);
        entityManager.persist(highTask);
        entityManager.persist(lowTask);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Task> result = taskRepository.findByFilters(null, TaskPriority.HIGH, null, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(TaskPriority.HIGH, result.getContent().get(0).getPriority());
    }

    @Test
    void findByFilters_FilterByAssignedUser_Success() {
        // Arrange
        User anotherUser = new User(null, "Jane Doe", "jane@example.com");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        Task task1 = createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        task1.setAssignedTo(testUser);

        Task task2 = createTask("Task 2", TaskStatus.TODO, TaskPriority.HIGH);
        task2.setAssignedTo(anotherUser);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Task> result = taskRepository.findByFilters(null, null, testUser.getId(), pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getId(), result.getContent().get(0).getAssignedTo().getId());
    }

    @Test
    void findByFilters_MultipleFilters_Success() {
        // Arrange
        Task matchingTask = createTask("Matching", TaskStatus.TODO, TaskPriority.HIGH);
        Task nonMatchingTask1 = createTask("Non-matching 1", TaskStatus.DONE, TaskPriority.HIGH);
        Task nonMatchingTask2 = createTask("Non-matching 2", TaskStatus.TODO, TaskPriority.LOW);

        entityManager.persist(matchingTask);
        entityManager.persist(nonMatchingTask1);
        entityManager.persist(nonMatchingTask2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Task> result = taskRepository.findByFilters(
                TaskStatus.TODO,
                TaskPriority.HIGH,
                testUser.getId(),
                pageable
        );

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Matching", result.getContent().get(0).getTitle());
    }

    @Test
    void findByFilters_WithPagination_Success() {
        // Arrange - Create 5 tasks
        for (int i = 1; i <= 5; i++) {
            Task task = createTask("Task " + i, TaskStatus.TODO, TaskPriority.HIGH);
            entityManager.persist(task);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2); // First page, 2 items

        // Act
        Page<Task> result = taskRepository.findByFilters(null, null, null, pageable);

        // Assert
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
    }

    @Test
    void delete_Success() {
        // Arrange
        Task savedTask = entityManager.persistAndFlush(testTask);
        Long taskId = savedTask.getId();

        // Act
        taskRepository.deleteById(taskId);
        entityManager.flush();

        // Assert
        var deletedTask = taskRepository.findById(taskId);
        assertFalse(deletedTask.isPresent());
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