package com.example.taskmanagement.services;

import com.example.taskmanagement.dto.TaskDTO;
import com.example.taskmanagement.dto.TaskStatusUpdateDTO;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.enums.TaskPriority;
import com.example.taskmanagement.enums.TaskStatus;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskDTO taskDTO;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setAssignedTo(user);

        taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setStatus(TaskStatus.TODO);
        taskDTO.setPriority(TaskPriority.HIGH);
        taskDTO.setDueDate(LocalDate.now().plusDays(7));
        taskDTO.setAssignedToId(1L);
    }

    @Test
    void createTask_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDTO result = taskService.createTask(taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getStatus(), result.getStatus());
        assertEquals(task.getPriority(), result.getPriority());
        assertEquals(user.getId(), result.getAssignedToId());

        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        taskDTO.setAssignedToId(999L);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(taskDTO)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_Success_WithoutAssignedUser() {
        // Arrange
        taskDTO.setAssignedToId(null);
        task.setAssignedTo(null);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDTO result = taskService.createTask(taskDTO);

        // Assert
        assertNotNull(result);
        assertNull(result.getAssignedToId());
        verify(userRepository, never()).findById(any());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getAllTasks_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(task));
        when(taskRepository.findByFilters(null, null, null, pageable)).thenReturn(taskPage);

        // Act
        Page<TaskDTO> result = taskService.getAllTasks(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(task.getTitle(), result.getContent().get(0).getTitle());

        verify(taskRepository, times(1)).findByFilters(null, null, null, pageable);
    }

    @Test
    void getAllTasks_WithFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(task));
        when(taskRepository.findByFilters(TaskStatus.TODO, TaskPriority.HIGH, 1L, pageable))
                .thenReturn(taskPage);

        // Act
        Page<TaskDTO> result = taskService.getAllTasks(TaskStatus.TODO, TaskPriority.HIGH, 1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(taskRepository, times(1)).findByFilters(TaskStatus.TODO, TaskPriority.HIGH, 1L, pageable);
    }

    @Test
    void getTaskById_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act
        TaskDTO result = taskService.getTaskById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getStatus(), result.getStatus());

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_ThrowsResourceNotFoundException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(999L)
        );

        assertTrue(exception.getMessage().contains("Task not found"));
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    void updateTask_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskDTO.setTitle("Updated Title");
        taskDTO.setStatus(TaskStatus.IN_PROGRESS);

        // Act
        TaskDTO result = taskService.updateTask(1L, taskDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskStatusUpdateDTO statusDTO = new TaskStatusUpdateDTO(TaskStatus.DONE);

        // Act
        TaskDTO result = taskService.updateTaskStatus(1L, statusDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void deleteTask_Success() {
        // Arrange
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_ThrowsResourceNotFoundException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.deleteTask(999L)
        );

        assertTrue(exception.getMessage().contains("Task not found"));
        verify(taskRepository, times(1)).existsById(999L);
        verify(taskRepository, never()).deleteById(any());
    }
}