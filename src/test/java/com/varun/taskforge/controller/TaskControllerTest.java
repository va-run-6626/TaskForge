package com.varun.taskforge.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end test against the real TaskServiceImpl, using a short duration.
 * MockMvc paths are relative to the servlet context, so /api/v1 is omitted.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void simulate_validRequest_runsTaskAndReturnsCompleted() throws Exception {
        mockMvc.perform(post("/tasks/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskName\":\"report\",\"durationMs\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value("report"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.threadName", notNullValue()))
                .andExpect(jsonPath("$.executionTimeMs", greaterThanOrEqualTo(100)));
    }
}
