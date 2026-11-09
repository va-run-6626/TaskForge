package com.varun.taskforge.dto;

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
    @NotBlank(message = "taskname cannot be blank")
    public String taskName;

    @NotNull(message = "durationMs is required")
    @Max(value = 10000, message = "durationMs must be between 100 and 10000")
    @Min(value = 100, message = "durationMs must be between 100 and 10000")
    public long durationMs;
}
