package com.varun.taskforge.exception;

/**
 * Thrown when a simulated task is interrupted before it finishes.
 * Translated to HTTP 503 Service Unavailable by {@link GlobalExceptionHandler}.
 */
public class TaskInterruptedException extends RuntimeException {

    public TaskInterruptedException(String taskName, InterruptedException cause) {
        super("Task '" + taskName + "' was interrupted before completion", cause);
    }
}
