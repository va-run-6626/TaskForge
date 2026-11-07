package com.varun.taskforge.service;

import com.varun.taskforge.dto.TaskSimulationReponseDTO;

/**
 * ITaskService
 */
public interface ITaskService {
    public TaskSimulationReponseDTO stimulate(String taskname, int durationMs);
}
