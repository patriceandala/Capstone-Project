package com.tana.migration.exception;

/**
 * Exception thrown when job data is invalid (missing required fields, invalid format, etc.).
 */
public class InvalidJobDataException extends DataAnomalyException {
    private final Integer jobId;
    private final String validationError;

    public InvalidJobDataException(String message, Integer jobId, String validationError) {
        super(message);
        this.jobId = jobId;
        this.validationError = validationError;
    }

    public InvalidJobDataException(String message, Integer jobId, String validationError, Throwable cause) {
        super(message, cause);
        this.jobId = jobId;
        this.validationError = validationError;
    }

    public Integer getJobId() {
        return jobId;
    }

    public String getValidationError() {
        return validationError;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Job ID: " + jobId + ", Error: " + validationError;
    }
}

