package com.tana.migration;

import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.model.CompetitorJob;
import com.tana.migration.model.RmjJob;

import java.util.List;

/**
 * Interface for mapping Competitor Data Model (CDM) to Redwood Migration Job (RMJ) format.
 * Handles transformation, data reconciliation, and circular dependency resolution.
 * 
 * Follows Single Responsibility Principle - responsible only for mapping/transformation.
 */
public interface CdmMapper {
    
    /**
     * Maps a list of competitor jobs to RMJ format.
     * Performs data reconciliation, cycle detection, and transformation.
     * 
     * @param competitorJobs List of jobs from competitor system
     * @return List of jobs in RMJ format
     * @throws DataAnomalyException if mapping cannot be completed due to data anomalies
     */
    List<RmjJob> mapToRmj(List<CompetitorJob> competitorJobs) throws DataAnomalyException;
    
    /**
     * Detects and reports circular dependencies in the job dependency graph.
     * 
     * @param competitorJobs List of jobs to analyze
     * @return List of cycle paths (each path is a list of job IDs forming a cycle)
     */
    List<List<Integer>> detectCircularDependencies(List<CompetitorJob> competitorJobs);
    
    /**
     * Reconciles contradictory data between different source files.
     * Based on Day 1 analysis - JSON-first strategy with XML as cross-reference.
     * 
     * @param jobsFromSource1 Jobs from first source (e.g., JSON)
     * @param jobsFromSource2 Jobs from second source (e.g., XML)
     * @return Reconciled list of jobs
     * @throws DataAnomalyException if reconciliation cannot resolve conflicts
     */
    List<CompetitorJob> reconcileData(List<CompetitorJob> jobsFromSource1, 
                                      List<CompetitorJob> jobsFromSource2) 
            throws DataAnomalyException;
}

