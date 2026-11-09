package com.varun.taskforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.varun.taskforge.dto.TaskSimulationRequestDTO;
import com.varun.taskforge.dto.TaskSimulationResponseDTO;
import com.varun.taskforge.exception.TaskInterruptedException;
import com.varun.taskforge.service.ITaskService;

/**
 * HTTP-layer tests: validation, error translation and status codes.
 * The service is mocked, so no test here sleeps; real execution timing is
 * covered by {@link TaskControllerTest} and TaskServiceImplTest.
 */
@WebMvcTest(TaskController.class)
class TaskControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITaskService taskService;

    private ResultActions postJson(String body) throws Exception {
        return mockMvc.perform(post("/tasks/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @ParameterizedTest
    @ValueSource(longs = {100, 10000})
    void simulate_boundaryDurations_areAccepted(long durationMs) throws Exception {
        when(taskService.simulate(any(TaskSimulationRequestDTO.class)))
                .thenReturn(TaskSimulationResponseDTO.builder()
                        .taskName("edge")
                        .status(TaskSimulationResponseDTO.Status.COMPLETED)
                        .threadName("test")
                        .executionTimeMs(durationMs)
                        .build());

        postJson("{\"taskName\":\"edge\",\"durationMs\":" + durationMs + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @ParameterizedTest
    @ValueSource(longs = {99, 10001, 0, -1})
    void simulate_outOfRangeDuration_returnsValidationError(long durationMs) throws Exception {
        postJson("{\"taskName\":\"edge\",\"durationMs\":" + durationMs + "}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("durationMs must be between 100 and 10000"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
        verify(taskService, never()).simulate(any());
    }

    @Test
    void simulate_missingDuration_returnsValidationError() throws Exception {
        postJson("{\"taskName\":\"report\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("durationMs is required"));
    }

    @Test
    void simulate_nullDuration_returnsValidationError() throws Exception {
        postJson("{\"taskName\":\"report\",\"durationMs\":null}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("durationMs is required"));
    }

    @Test
    void simulate_blankTaskName_returnsValidationError() throws Exception {
        postJson("{\"taskName\":\"  \",\"durationMs\":100}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("taskName cannot be blank"));
    }

    @Test
    void simulate_malformedJson_returnsBadRequest() throws Exception {
        postJson("{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
    }

    @Test
    void simulate_interruptedTask_returns503() throws Exception {
        when(taskService.simulate(any(TaskSimulationRequestDTO.class)))
                .thenThrow(new TaskInterruptedException("report", new InterruptedException()));

        postJson("{\"taskName\":\"report\",\"durationMs\":1000}")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("TASK_INTERRUPTED"))
                .andExpect(jsonPath("$.message").value("Task 'report' was interrupted before completion"));
    }
}
