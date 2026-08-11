package com.example.taskflow.dto;

public class TaskSummaryResponse {

    public static class ByStatus {
        private final int todo;
        private final int inProgress;
        private final int done;

        public ByStatus(int todo, int inProgress, int done) {
            this.todo = todo;
            this.inProgress = inProgress;
            this.done = done;
        }

        public int getTodo() { return todo; }
        public int getInProgress() { return inProgress; }
        public int getDone() { return done; }
    }

    public static class ByPriority {
        private final int high;
        private final int medium;
        private final int low;

        public ByPriority(int high, int medium, int low) {
            this.high = high;
            this.medium = medium;
            this.low = low;
        }

        public int getHigh() { return high; }
        public int getMedium() { return medium; }
        public int getLow() { return low; }
    }

    private final int totalTasks;
    private final ByStatus byStatus;
    private final ByPriority byPriority;
    private final double completionRate;
    private final int overdueCount;
    private final int tasksThisWeek;
    private final int dueToday;

    public TaskSummaryResponse(
            int totalTasks,
            ByStatus byStatus,
            ByPriority byPriority,
            double completionRate,
            int overdueCount,
            int tasksThisWeek,
            int dueToday
    ) {
        this.totalTasks = totalTasks;
        this.byStatus = byStatus;
        this.byPriority = byPriority;
        this.completionRate = completionRate;
        this.overdueCount = overdueCount;
        this.tasksThisWeek = tasksThisWeek;
        this.dueToday = dueToday;
    }

    public int getTotalTasks() { return totalTasks; }
    public ByStatus getByStatus() { return byStatus; }
    public ByPriority getByPriority() { return byPriority; }
    public double getCompletionRate() { return completionRate; }
    public int getOverdueCount() { return overdueCount; }
    public int getTasksThisWeek() { return tasksThisWeek; }
    public int getDueToday() { return dueToday; }
}