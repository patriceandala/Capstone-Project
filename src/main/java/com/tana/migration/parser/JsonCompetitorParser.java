package com.tana.migration.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tana.migration.CompetitorParser;
import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.exception.InvalidJobDataException;
import com.tana.migration.model.CompetitorJob;
import com.tana.migration.model.JobDependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JSON parser implementation for competitor data files.
 * Handles export_A_with_circular_dependency.json format.
 * 
 * Follows Single Responsibility Principle - only handles JSON parsing.
 */
public class JsonCompetitorParser implements CompetitorParser {
    
    private final ObjectMapper objectMapper;
    
    public JsonCompetitorParser() {
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public List<CompetitorJob> parse(String filePath) throws DataAnomalyException {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new InvalidJobDataException("File not found: " + filePath, null, "File does not exist");
            }
            
            // Parse JSON array of jobs
            CompetitorJob[] jobsArray = objectMapper.readValue(file, CompetitorJob[].class);
            List<CompetitorJob> jobs = new ArrayList<>();
            
            for (CompetitorJob job : jobsArray) {
                if (job != null) {
                    job.setSourceFile(filePath);
                    jobs.add(job);
                }
            }
            
            return jobs;
            
        } catch (IOException e) {
            throw new InvalidJobDataException("Failed to parse JSON file: " + filePath, null, e.getMessage(), e);
        }
    }
    
    @Override
    public void validate(List<CompetitorJob> jobs) throws DataAnomalyException {
        if (jobs == null || jobs.isEmpty()) {
            throw new InvalidJobDataException("No jobs found in data", null, "Empty job list");
        }
        
        Set<Integer> jobIds = new HashSet<>();
        List<String> errors = new ArrayList<>();
        
        for (CompetitorJob job : jobs) {
            // Validate required fields
            if (job.getJobId() == null) {
                errors.add("Job missing required field: job_id");
                continue;
            }
            
            if (job.getJobName() == null || job.getJobName().trim().isEmpty()) {
                errors.add("Job " + job.getJobId() + " missing required field: job_name");
            }
            
            // Check for duplicate job IDs
            if (jobIds.contains(job.getJobId())) {
                errors.add("Duplicate job_id found: " + job.getJobId());
            } else {
                jobIds.add(job.getJobId());
            }
            
            // Validate dependencies reference existing jobs
            if (job.getDependencies() != null) {
                for (JobDependency dep : job.getDependencies()) {
                    if (dep.getDependentJobId() == null) {
                        errors.add("Job " + job.getJobId() + " has dependency with null job_id");
                    }
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new InvalidJobDataException(
                "Validation failed: " + String.join("; ", errors),
                null,
                String.join("; ", errors)
            );
        }
    }
}

