package com.varun.taskforge.system.application;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.varun.taskforge.system.api.dto.SystemResponseDTO;

@Service
public class SystemService {

    private final String applicationName;
    private final String javaVersion;

    public SystemService(
            @Value("${spring.application.name}") String applicationName,
            @Value("${app.java.version}") String javaVersion) {
        this.applicationName = applicationName;
        this.javaVersion = javaVersion;
    }

    public SystemResponseDTO getSystemInformation() {
        return SystemResponseDTO.builder()
                .applicationName(applicationName)
                .javaVersion(javaVersion)
                .threadName(Thread.currentThread().getName())
                .availableProcessors(Runtime.getRuntime().availableProcessors())
                .timeStamp(Instant.now())
                .build();
    }
}
