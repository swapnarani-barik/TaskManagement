package com.example.taskflow.dto;

public class TimeTotalResponse {
    private int totalMinutes;

    public TimeTotalResponse(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }
}

