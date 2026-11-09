package com.varun.taskforge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.varun.taskforge.dto.TaskSimulationRequestDTO;
import com.varun.taskforge.dto.TaskSimulationResponseDTO;
import com.varun.taskforge.service.ITaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@RequestMapping("/tasks")
public class TaskController {
    private final ITaskService taskService;

    @PostMapping("/simulate")
    public ResponseEntity<TaskSimulationResponseDTO> simulate(@Valid @RequestBody  TaskSimulationRequestDTO requestDTO){
        TaskSimulationResponseDTO response = taskService.simulate(requestDTO);
        return ResponseEntity.ok(response);
    }
}
