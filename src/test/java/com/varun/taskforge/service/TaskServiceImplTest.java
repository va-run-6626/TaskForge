package com.varun.taskforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.varun.taskforge.dto.TaskSimulationRequestDTO;
import com.varun.taskforge.dto.TaskSimulationResponseDTO;
import com.varun.taskforge.exception.TaskInterruptedException;

class TaskServiceImplTest {

    private final TaskServiceImpl service = new TaskServiceImpl();

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    private static TaskSimulationRequestDTO request(long durationMs) {
        return TaskSimulationRequestDTO.builder().taskName("t").durationMs(durationMs).build();
    }

    @Test
    void simulate_completesAndMeasuresElapsedTime() {
        TaskSimulationResponseDTO response = service.simulate(request(150));

        assertThat(response.getStatus()).isEqualTo(TaskSimulationResponseDTO.Status.COMPLETED);
        assertThat(response.getTaskName()).isEqualTo("t");
        assertThat(response.getThreadName()).isEqualTo(Thread.currentThread().getName());
        assertThat(response.getExecutionTimeMs()).isGreaterThanOrEqualTo(150);
    }

    @Test
    void simulate_alreadyInterruptedThread_throwsAndRestoresFlag() {
        // Thread.sleep throws immediately if the interrupt flag is already set.
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> service.simulate(request(5000)))
                .isInstanceOf(TaskInterruptedException.class)
                .hasCauseInstanceOf(InterruptedException.class);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    @Test
    void simulate_interruptedWhileSleeping_throwsWithoutCompleting() throws Exception {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicReference<TaskSimulationResponseDTO> result = new AtomicReference<>();
        AtomicBoolean flagRestored = new AtomicBoolean();
        CountDownLatch started = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            started.countDown();
            try {
                result.set(service.simulate(request(10_000)));
            } catch (Throwable t) {
                thrown.set(t);
                flagRestored.set(Thread.currentThread().isInterrupted());
            }
        }, "interrupt-test-worker");

        worker.start();
        assertThat(started.await(1, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(100); // let the worker enter Thread.sleep
        worker.interrupt();
        worker.join(2_000);

        assertThat(worker.isAlive()).isFalse();
        assertThat(result.get()).isNull();
        assertThat(thrown.get()).isInstanceOf(TaskInterruptedException.class);
        assertThat(flagRestored.get()).isTrue();
    }
}
