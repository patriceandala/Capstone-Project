package com.tana.migration.exception;

/**
 * Base exception for all data anomalies encountered during parsing and migration.
 * Based on Day 1 analysis - various data quality issues identified.
 */
public class DataAnomalyException extends Exception {
    public DataAnomalyException(String message) {
        super(message);
    }

    public DataAnomalyException(String message, Throwable cause) {
        super(message, cause);
    }
}

