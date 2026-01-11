package com.example.taskmanagement.dto;

import com.example.taskmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateDTO {
    @NotNull(message = "Status is required")
    private TaskStatus status;
}