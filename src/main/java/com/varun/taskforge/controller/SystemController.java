package com.varun.taskforge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.varun.taskforge.dto.SystemResponseDTO;
import com.varun.taskforge.service.ISystemService;

@RestController 
@RequestMapping ("/system")
public class SystemController {

    private final ISystemService systemService;

    @Autowired 
    public SystemController(
        ISystemService systemService
    ) {
        this.systemService = systemService;
    }
    
    @GetMapping("/info")
    public SystemResponseDTO getSystemInformation(){
        return systemService.getSystemInformation();
    }
}