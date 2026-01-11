package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskDTO;
import com.example.taskmanagement.dto.TaskStatusUpdateDTO;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.enums.TaskPriority;
import com.example.taskmanagement.enums.TaskStatus;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.TODO);
        task.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.MEDIUM);
        task.setDueDate(taskDTO.getDueDate());

        if (taskDTO.getAssignedToId() != null) {
            User user = userRepository.findById(taskDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + taskDTO.getAssignedToId()));
            task.setAssignedTo(user);
        }

        Task savedTask = taskRepository.save(task);
        return toDTO(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> getAllTasks(TaskStatus status, TaskPriority priority, Long assignedToId, Pageable pageable) {
        return taskRepository.findByFilters(status, priority, assignedToId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return toDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : task.getStatus());
        task.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : task.getPriority());
        task.setDueDate(taskDTO.getDueDate());

        if (taskDTO.getAssignedToId() != null) {
            User user = userRepository.findById(taskDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + taskDTO.getAssignedToId()));
            task.setAssignedTo(user);
        } else {
            task.setAssignedTo(null);
        }

        Task updatedTask = taskRepository.save(task);
        return toDTO(updatedTask);
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long id, TaskStatusUpdateDTO statusDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setStatus(statusDTO.getStatus());
        Task updatedTask = taskRepository.save(task);
        return toDTO(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        if (task.getAssignedTo() != null) {
            dto.setAssignedToId(task.getAssignedTo().getId());
            dto.setAssignedToName(task.getAssignedTo().getName());
        }

        return dto;
    }
}