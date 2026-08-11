package com.example.taskflow.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    HIGH,
    MED,
    MEDIUM,
    LOW;

    @JsonCreator
    public static Priority fromValue(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        if ("MEDIUM".equals(normalized)) return MED;
        return Priority.valueOf(normalized);
    }

    @JsonValue
    public String toValue() {
        if (this == MED || this == MEDIUM) return "MEDIUM";
        return name();
    }

    public static Priority forPersistence(Priority priority) {
        if (priority == null || priority == MEDIUM) {
            return MED;
        }
        return priority;
    }
}
