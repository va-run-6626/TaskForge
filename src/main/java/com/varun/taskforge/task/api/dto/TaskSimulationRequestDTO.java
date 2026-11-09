package com.varun.taskforge.task.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSimulationRequestDTO {
    @NotBlank(message = "taskName cannot be blank")
    private String taskName;

    // Boxed Long so that a missing or explicit null value can be rejected by @NotNull.
    @NotNull(message = "durationMs is required")
    @Min(value = 100, message = "durationMs must be between 100 and 10000")
    @Max(value = 10000, message = "durationMs must be between 100 and 10000")
    private Long durationMs;
}
