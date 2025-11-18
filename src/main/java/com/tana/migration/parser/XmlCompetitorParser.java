package com.tana.migration.parser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
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
 * XML parser implementation for competitor data files.
 * Handles export_B_contradictory.xml format.
 * 
 * Follows Single Responsibility Principle - only handles XML parsing.
 */
public class XmlCompetitorParser implements CompetitorParser {
    
    private final XmlMapper xmlMapper;
    
    public XmlCompetitorParser() {
        this.xmlMapper = new XmlMapper();
    }
    
    @Override
    public List<CompetitorJob> parse(String filePath) throws DataAnomalyException {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new InvalidJobDataException("File not found: " + filePath, null, "File does not exist");
            }
            
            // Parse XML ProcessChain
            ProcessChain processChain = xmlMapper.readValue(file, ProcessChain.class);
            List<CompetitorJob> jobs = new ArrayList<>();
            
            if (processChain != null && processChain.getJobs() != null) {
                for (XmlJob xmlJob : processChain.getJobs()) {
                    CompetitorJob job = new CompetitorJob();
                    job.setJobId(xmlJob.getId());
                    job.setJobName(xmlJob.getName());
                    job.setSourceFile(filePath);
                    
                    // XML only supports single dependency via dependsOn attribute
                    if (xmlJob.getDependsOn() != null) {
                        JobDependency dependency = new JobDependency(xmlJob.getDependsOn());
                        job.addDependency(dependency);
                    }
                    
                    jobs.add(job);
                }
            }
            
            return jobs;
            
        } catch (IOException e) {
            throw new InvalidJobDataException("Failed to parse XML file: " + filePath, null, e.getMessage(), e);
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
                errors.add("Job missing required field: id");
                continue;
            }
            
            if (job.getJobName() == null || job.getJobName().trim().isEmpty()) {
                errors.add("Job " + job.getJobId() + " missing required field: name");
            }
            
            // Check for duplicate job IDs
            if (jobIds.contains(job.getJobId())) {
                errors.add("Duplicate job id found: " + job.getJobId());
            } else {
                jobIds.add(job.getJobId());
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
    
    // Inner classes for XML structure mapping
    @JacksonXmlRootElement(localName = "ProcessChain")
    private static class ProcessChain {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Job")
        private List<XmlJob> jobs;
        
        public List<XmlJob> getJobs() {
            return jobs;
        }
        
        public void setJobs(List<XmlJob> jobs) {
            this.jobs = jobs;
        }
    }
    
    private static class XmlJob {
        @JacksonXmlProperty(isAttribute = true, localName = "id")
        private Integer id;
        
        @JacksonXmlProperty(isAttribute = true, localName = "name")
        private String name;
        
        @JacksonXmlProperty(isAttribute = true, localName = "dependsOn")
        private Integer dependsOn;
        
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Integer getDependsOn() {
            return dependsOn;
        }
        
        public void setDependsOn(Integer dependsOn) {
            this.dependsOn = dependsOn;
        }
    }
}

