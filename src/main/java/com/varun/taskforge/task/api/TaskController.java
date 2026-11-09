package com.varun.taskforge.task.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.varun.taskforge.task.api.dto.TaskSimulationRequestDTO;
import com.varun.taskforge.task.api.dto.TaskSimulationResponseDTO;
import com.varun.taskforge.task.application.TaskSimulationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@RequestMapping("/tasks")
public class TaskController {
    private final TaskSimulationService taskService;

    @PostMapping("/simulate")
    public ResponseEntity<TaskSimulationResponseDTO> simulate(@Valid @RequestBody  TaskSimulationRequestDTO requestDTO){
        TaskSimulationResponseDTO response = taskService.simulate(requestDTO);
        return ResponseEntity.ok(response);
    }
}
