package com.varun.taskforge.task.exception;

/**
 * Thrown when a simulated task is interrupted before it finishes.
 * Translated to HTTP 503 Service Unavailable by the shared web exception handler.
 */
public class TaskInterruptedException extends RuntimeException {

    public TaskInterruptedException(String taskName, InterruptedException cause) {
        super("Task '" + taskName + "' was interrupted before completion", cause);
    }
}
