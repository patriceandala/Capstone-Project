package com.tana.migration.exception;

/**
 * Exception thrown when contradictory data is found between different source files.
 * Based on Day 1 analysis - Job 4100 has different dependencies in JSON vs XML.
 */
public class ContradictoryDataException extends DataAnomalyException {
    private final Integer jobId;
    private final String source1;
    private final String source2;
    private final String conflictDescription;

    public ContradictoryDataException(String message, Integer jobId, String source1, String source2, String conflictDescription) {
        super(message);
        this.jobId = jobId;
        this.source1 = source1;
        this.source2 = source2;
        this.conflictDescription = conflictDescription;
    }

    public ContradictoryDataException(String message, Integer jobId, String source1, String source2, String conflictDescription, Throwable cause) {
        super(message, cause);
        this.jobId = jobId;
        this.source1 = source1;
        this.source2 = source2;
        this.conflictDescription = conflictDescription;
    }

    public Integer getJobId() {
        return jobId;
    }

    public String getSource1() {
        return source1;
    }

    public String getSource2() {
        return source2;
    }

    public String getConflictDescription() {
        return conflictDescription;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + 
               " Job ID: " + jobId + 
               ", Sources: " + source1 + " vs " + source2 + 
               ", Conflict: " + conflictDescription;
    }
}

