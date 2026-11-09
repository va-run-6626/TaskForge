package com.varun.taskforge.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * End-to-end API tests against the real TaskServiceImpl.
 * MockMvc paths are relative to the servlet context, so /api/v1 is omitted.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions postJson(String body) throws Exception {
        return mockMvc.perform(post("/tasks/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void simulate_validRequest_returnsCompleted() throws Exception {
        postJson("{\"taskName\":\"report\",\"durationMs\":100}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value("report"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.threadName", notNullValue()))
                .andExpect(jsonPath("$.executionTimeMs", greaterThanOrEqualTo(100)));
    }

    @ParameterizedTest
    @ValueSource(longs = {100, 10000})
    void simulate_boundaryDurations_areAccepted(long durationMs) throws Exception {
        // 10000 ms keeps this test slow by design: it proves the upper bound is inclusive.
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
}
