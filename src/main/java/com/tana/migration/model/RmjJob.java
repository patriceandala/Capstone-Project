package com.tana.migration.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a job in the Redwood Migration Job (RMJ) format.
 * This is the target format after transformation.
 */
public class RmjJob {
    private Integer jobId;
    private String jobName;
    private List<Integer> dependencyJobIds;
    private String notes;
    private String triggerType; // From Phase 2 - trigger type mapping

    public RmjJob() {
        this.dependencyJobIds = new ArrayList<>();
    }

    public RmjJob(Integer jobId, String jobName) {
        this();
        this.jobId = jobId;
        this.jobName = jobName;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<Integer> getDependencyJobIds() {
        return dependencyJobIds;
    }

    public void setDependencyJobIds(List<Integer> dependencyJobIds) {
        this.dependencyJobIds = dependencyJobIds != null ? dependencyJobIds : new ArrayList<>();
    }

    public void addDependencyJobId(Integer jobId) {
        if (jobId != null) {
            this.dependencyJobIds.add(jobId);
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RmjJob rmjJob = (RmjJob) o;
        return Objects.equals(jobId, rmjJob.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return "RmjJob{" +
                "jobId=" + jobId +
                ", jobName='" + jobName + '\'' +
                ", dependencyJobIds=" + dependencyJobIds +
                ", triggerType='" + triggerType + '\'' +
                '}';
    }
}

