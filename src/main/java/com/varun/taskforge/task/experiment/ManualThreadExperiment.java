package com.varun.taskforge.task.experiment;

public class ManualThreadExperiment {
    public static void main(String[] args) throws InterruptedException {
        StatusTask[] tasks = new StatusTask[3];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new StatusTask();
        }
        Thread[] workers = new Thread[3];
        for (int i = 0; i < workers.length; i++) {
            String name = "worker-" + i;
            workers[i] = new Thread(tasks[i], name);
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < workers.length; i++) {
            workers[i].start();
        }
        workers[1].interrupt();
        for (int i = 0; i < workers.length; i++) {
            workers[i].join();
        }

        long totalTime = (System.nanoTime() - startTime) / 1_000_000;
        int[] stateCnt = new int[5];
        for (int i = 0; i < workers.length; i++) {
            StatusTask.State currState = tasks[i].getState();

            System.out.printf("%s | thread=%s | task=%s%n", workers[i].getName(), workers[i].getState(), currState);

            switch (currState) {
            case StatusTask.State.PENDING: {
                stateCnt[0]++;
                break;
            }
            case StatusTask.State.IN_PROGRESS: {
                stateCnt[1]++;
                break;
            }
            case StatusTask.State.INTERRUPTED: {
                stateCnt[2]++;
                break;
            }
            case StatusTask.State.COMPLETED: {
                stateCnt[3]++;
                break;
            }
            case StatusTask.State.FAILED: {
                stateCnt[4]++;
                break;
            }
            }
        }
        System.out.println(stateCnt[3] + " : Completed, " + stateCnt[2] + " : Interrupted, " + stateCnt[4]
                + " : Failed, " + stateCnt[1] + " : In progress, " + stateCnt[0] + " : Pending ");

        System.out.println("Time Taken : " + totalTime);
    }
}

class StatusTask implements Runnable {

    private State state;

    public StatusTask() {
        this.state = State.PENDING;
    }

    @Override
    public void run() {
        try {
            this.state = State.IN_PROGRESS;
            if (Thread.currentThread().getName().equals("worker-2")) {
                System.out.println("Stimulated Failure");
                throw new IllegalStateException("stimulated Failure");
            }
            System.out.println("Worker : " + Thread.currentThread().getName());
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            this.state = State.INTERRUPTED;
            Thread.currentThread().interrupt();
            return;
        } catch (IllegalStateException e) {
            this.state = State.FAILED;
            return;
        }
        this.state = State.COMPLETED;
    }

    public State getState() {
        return this.state;
    }

    enum State {
        PENDING, COMPLETED, INTERRUPTED, IN_PROGRESS, FAILED
    }
}
