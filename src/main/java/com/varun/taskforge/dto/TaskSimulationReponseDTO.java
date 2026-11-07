package com.varun.taskforge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * TaskSimulationReponse
 */
public class TaskSimulationReponseDTO {
    @NotBlank 
    public String taskName;

    @Max(10000) @Min(100)
    public int durationMs;
}
