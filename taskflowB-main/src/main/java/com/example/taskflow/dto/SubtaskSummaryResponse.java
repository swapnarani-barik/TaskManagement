package com.example.taskflow.dto;

public class SubtaskSummaryResponse {
    private int total;
    private int completed;

    public SubtaskSummaryResponse(int total, int completed) {
        this.total = total;
        this.completed = completed;
    }

    public int getTotal() {
        return total;
    }

    public int getCompleted() {
        return completed;
    }
}

