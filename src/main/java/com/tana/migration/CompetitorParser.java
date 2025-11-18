package com.tana.migration;

import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.model.CompetitorJob;

import java.util.List;

/**
 * Interface for parsing competitor data files (JSON and XML formats).
 * Based on Day 1 analysis - supports both export_A_with_circular_dependency.json
 * and export_B_contradictory.xml formats.
 * 
 * Follows Single Responsibility Principle - responsible only for parsing.
 */
public interface CompetitorParser {
    
    /**
     * Parses a competitor data file and returns a list of jobs.
     * 
     * @param filePath Path to the competitor data file (JSON or XML)
     * @return List of parsed CompetitorJob objects
     * @throws DataAnomalyException if data anomalies are detected during parsing
     */
    List<CompetitorJob> parse(String filePath) throws DataAnomalyException;
    
    /**
     * Validates that the parsed data meets basic requirements.
     * 
     * @param jobs List of jobs to validate
     * @throws DataAnomalyException if validation fails
     */
    void validate(List<CompetitorJob> jobs) throws DataAnomalyException;
}

