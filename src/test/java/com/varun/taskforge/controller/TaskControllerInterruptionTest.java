package com.varun.taskforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.varun.taskforge.dto.TaskSimulationRequestDTO;
import com.varun.taskforge.exception.TaskInterruptedException;
import com.varun.taskforge.service.ITaskService;

/** Verifies that an interrupted task is translated to HTTP 503. */
@WebMvcTest(TaskController.class)
class TaskControllerInterruptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITaskService taskService;

    @Test
    void simulate_interruptedTask_returns503() throws Exception {
        when(taskService.simulate(any(TaskSimulationRequestDTO.class)))
                .thenThrow(new TaskInterruptedException("report", new InterruptedException()));

        mockMvc.perform(post("/tasks/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskName\":\"report\",\"durationMs\":1000}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("TASK_INTERRUPTED"))
                .andExpect(jsonPath("$.message").value("Task 'report' was interrupted before completion"));
    }
}
