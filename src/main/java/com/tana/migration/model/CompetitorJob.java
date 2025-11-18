package com.tana.migration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a job/process from the competitor's system.
 * Based on Day 1 analysis of competitor data files.
 */
public class CompetitorJob {
    @JsonProperty("job_id")
    private Integer jobId;
    
    @JsonProperty("job_name")
    private String jobName;
    
    private List<JobDependency> dependencies;
    private String notes;
    private String sourceFile; // Track which file this came from (JSON or XML)

    public CompetitorJob() {
        this.dependencies = new ArrayList<>();
    }

    public CompetitorJob(Integer jobId, String jobName) {
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

    public List<JobDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<JobDependency> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }

    public void addDependency(JobDependency dependency) {
        if (dependency != null) {
            this.dependencies.add(dependency);
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetitorJob that = (CompetitorJob) o;
        return Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return "CompetitorJob{" +
                "jobId=" + jobId +
                ", jobName='" + jobName + '\'' +
                ", dependencies=" + dependencies.size() +
                ", sourceFile='" + sourceFile + '\'' +
                '}';
    }
}

