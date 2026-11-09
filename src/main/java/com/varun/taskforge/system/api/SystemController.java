package com.varun.taskforge.system.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.varun.taskforge.system.api.dto.SystemResponseDTO;
import com.varun.taskforge.system.application.SystemService;

@RestController
@RequestMapping("/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/info")
    public SystemResponseDTO getSystemInformation() {
        return systemService.getSystemInformation();
    }
}
