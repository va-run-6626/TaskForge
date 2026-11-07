package com.varun.taskforge.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.varun.taskforge.dto.SystemResponseDTO;

@Service 
public class SystemServiceImpl implements ISystemService {
    
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.java.version}")
    private String javaVersion;

    @Override
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
