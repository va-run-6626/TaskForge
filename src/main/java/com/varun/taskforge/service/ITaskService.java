package com.varun.taskforge.service;

import com.varun.taskforge.dto.TaskSimulationRequestDTO;
import com.varun.taskforge.dto.TaskSimulationResponseDTO;

/**
 * ITaskService
 */
public interface ITaskService {
    public TaskSimulationResponseDTO simulate(TaskSimulationRequestDTO request);
}
