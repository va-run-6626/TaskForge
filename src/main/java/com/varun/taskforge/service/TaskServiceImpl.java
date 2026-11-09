package com.varun.taskforge.service;

import org.springframework.stereotype.Service;

import com.varun.taskforge.dto.TaskSimulationRequestDTO;
import com.varun.taskforge.dto.TaskSimulationResponseDTO;
import com.varun.taskforge.exception.TaskInterruptedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskServiceImpl implements ITaskService {

    @Override
    public TaskSimulationResponseDTO simulate(TaskSimulationRequestDTO request) {
        String threadName = Thread.currentThread().getName();
        log.info("Starting task '{}' on thread [{}] for {} ms", request.getTaskName(), threadName,
                request.getDurationMs());
        long startTime = System.nanoTime();
        try {
            Thread.sleep(request.getDurationMs());
        } catch (InterruptedException e) {
            // Restore the interrupt flag so callers further up the stack can still observe it,
            // then abort the normal path: an interrupted task must never be reported as COMPLETED.
            Thread.currentThread().interrupt();
            log.error("Task '{}' on thread [{}] was interrupted", request.getTaskName(), threadName);
            throw new TaskInterruptedException(request.getTaskName(), e);
        }
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Completed task '{}' on thread [{}] in {} ms", request.getTaskName(), threadName, elapsedMs);

        return TaskSimulationResponseDTO.builder()
                .taskName(request.getTaskName())
                .status(TaskSimulationResponseDTO.Status.COMPLETED)
                .threadName(threadName)
                .executionTimeMs(elapsedMs)
                .build();
    }
}
