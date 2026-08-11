package com.example.taskflow.dto;

import java.time.Instant;

public class ActiveTimerResponse {
    private boolean running;
    private Instant startTime;

    public ActiveTimerResponse(boolean running, Instant startTime) {
        this.running = running;
        this.startTime = startTime;
    }

    public boolean isRunning() {
        return running;
    }

    public Instant getStartTime() {
        return startTime;
    }
}

