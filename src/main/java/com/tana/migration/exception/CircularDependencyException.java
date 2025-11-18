package com.tana.migration.exception;

import java.util.List;

/**
 * Exception thrown when a circular dependency is detected in the job dependency graph.
 * Based on Day 1 analysis - circular dependency found: 2001 → 4100 → 3200 → 3100 → 2001
 */
public class CircularDependencyException extends DataAnomalyException {
    private final List<Integer> cyclePath;

    public CircularDependencyException(String message, List<Integer> cyclePath) {
        super(message);
        this.cyclePath = cyclePath;
    }

    public CircularDependencyException(String message, List<Integer> cyclePath, Throwable cause) {
        super(message, cause);
        this.cyclePath = cyclePath;
    }

    public List<Integer> getCyclePath() {
        return cyclePath;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Cycle path: " + cyclePath;
    }
}

