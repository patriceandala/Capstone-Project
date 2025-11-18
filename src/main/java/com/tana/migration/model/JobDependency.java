package com.tana.migration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a dependency relationship between jobs.
 * Based on Day 1 analysis - JSON format includes status, XML format does not.
 */
public class JobDependency {
    @JsonProperty("job_id")
    private Integer dependentJobId;
    
    private String status; // e.g., "Success" - only in JSON format

    public JobDependency() {
    }

    public JobDependency(Integer dependentJobId) {
        this.dependentJobId = dependentJobId;
    }

    public JobDependency(Integer dependentJobId, String status) {
        this.dependentJobId = dependentJobId;
        this.status = status;
    }

    public Integer getDependentJobId() {
        return dependentJobId;
    }

    public void setDependentJobId(Integer dependentJobId) {
        this.dependentJobId = dependentJobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobDependency that = (JobDependency) o;
        return Objects.equals(dependentJobId, that.dependentJobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependentJobId);
    }

    @Override
    public String toString() {
        return "JobDependency{" +
                "dependentJobId=" + dependentJobId +
                ", status='" + status + '\'' +
                '}';
    }
}

