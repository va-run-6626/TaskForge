package com.varun.taskforge.system.api.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class SystemResponseDTO {
    private String applicationName;
    private String javaVersion;
    private String threadName;
    private Integer availableProcessors;
    private Instant timeStamp;
}