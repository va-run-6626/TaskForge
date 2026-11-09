package com.varun.taskforge.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSimulationResponseDTO {
    private String taskName;
    private Status status;
    private String threadName;
    private long exectutionTimeInMs;

    public enum Status {
        PENDING, RUNNING, COMPLETED, FAILED
    }
}
