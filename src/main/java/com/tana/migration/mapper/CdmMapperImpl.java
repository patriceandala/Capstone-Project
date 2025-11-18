package com.tana.migration.mapper;

import com.tana.migration.CdmMapper;
import com.tana.migration.exception.CircularDependencyException;
import com.tana.migration.exception.ContradictoryDataException;
import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.model.CompetitorJob;
import com.tana.migration.model.JobDependency;
import com.tana.migration.model.RmjJob;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Implementation of CdmMapper interface.
 * Maps competitor jobs to RMJ format with cycle detection and data reconciliation.
 * 
 * Uses JGraphT for cycle detection and follows SOLID principles.
 */
public class CdmMapperImpl implements CdmMapper {
    
    @Override
    public List<RmjJob> mapToRmj(List<CompetitorJob> competitorJobs) throws DataAnomalyException {
        if (competitorJobs == null || competitorJobs.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Detect circular dependencies first
        List<List<Integer>> cycles = detectCircularDependencies(competitorJobs);
        if (!cycles.isEmpty()) {
            throw new CircularDependencyException(
                "Circular dependencies detected. Cannot map to RMJ format.",
                cycles.get(0) // Throw exception with first cycle found
            );
        }
        
        // Map to RMJ format
        List<RmjJob> rmjJobs = new ArrayList<>();
        Map<Integer, CompetitorJob> jobMap = new HashMap<>();
        
        // Build job map for quick lookup
        for (CompetitorJob competitorJob : competitorJobs) {
            jobMap.put(competitorJob.getJobId(), competitorJob);
        }
        
        for (CompetitorJob competitorJob : competitorJobs) {
            RmjJob rmjJob = new RmjJob();
            rmjJob.setJobId(competitorJob.getJobId());
            rmjJob.setJobName(competitorJob.getJobName());
            rmjJob.setNotes(competitorJob.getNotes());
            
            // Map dependencies - extract job IDs from dependency objects
            if (competitorJob.getDependencies() != null) {
                for (JobDependency dep : competitorJob.getDependencies()) {
                    if (dep.getDependentJobId() != null) {
                        // Validate dependency reference exists
                        if (jobMap.containsKey(dep.getDependentJobId())) {
                            rmjJob.addDependencyJobId(dep.getDependentJobId());
                        }
                    }
                }
            }
            
            // Default trigger type (can be customized based on Phase 2 requirements)
            rmjJob.setTriggerType("MANUAL");
            
            rmjJobs.add(rmjJob);
        }
        
        return rmjJobs;
    }
    
    @Override
    public List<List<Integer>> detectCircularDependencies(List<CompetitorJob> competitorJobs) {
        List<List<Integer>> cycles = new ArrayList<>();
        
        if (competitorJobs == null || competitorJobs.isEmpty()) {
            return cycles;
        }
        
        // Build directed graph using JGraphT
        Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        // Add all job IDs as vertices
        for (CompetitorJob job : competitorJobs) {
            if (job.getJobId() != null) {
                graph.addVertex(job.getJobId());
            }
        }
        
        // Add edges for dependencies
        for (CompetitorJob job : competitorJobs) {
            if (job.getJobId() != null && job.getDependencies() != null) {
                for (JobDependency dep : job.getDependencies()) {
                    if (dep.getDependentJobId() != null) {
                        // Add edge: job depends on dependentJobId
                        // So edge goes from dependentJobId to job (reverse direction for dependency)
                        if (graph.containsVertex(dep.getDependentJobId())) {
                            graph.addEdge(dep.getDependentJobId(), job.getJobId());
                        }
                    }
                }
            }
        }
        
        // Detect cycles using JGraphT CycleDetector
        CycleDetector<Integer, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        
        if (cycleDetector.detectCycles()) {
            // Find all cycles
            Set<Integer> cycleVertices = cycleDetector.findCycles();
            
            // Build cycle paths (simplified - JGraphT doesn't directly give paths)
            // We'll use DFS to find actual cycle paths
            for (Integer vertex : cycleVertices) {
                List<Integer> cyclePath = findCyclePath(graph, vertex, new HashSet<>(), new ArrayList<>());
                if (cyclePath != null && !cyclePath.isEmpty()) {
                    cycles.add(cyclePath);
                }
            }
        }
        
        return cycles;
    }
    
    /**
     * Helper method to find a cycle path starting from a given vertex using DFS.
     */
    private List<Integer> findCyclePath(Graph<Integer, DefaultEdge> graph, Integer startVertex,
                                       Set<Integer> visited, List<Integer> path) {
        if (visited.contains(startVertex)) {
            // Found a cycle
            int cycleStartIndex = path.indexOf(startVertex);
            if (cycleStartIndex >= 0) {
                List<Integer> cycle = new ArrayList<>(path.subList(cycleStartIndex, path.size()));
                cycle.add(startVertex); // Complete the cycle
                return cycle;
            }
            return null;
        }
        
        visited.add(startVertex);
        path.add(startVertex);
        
        // Check all outgoing edges
        Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(startVertex);
        for (DefaultEdge edge : outgoingEdges) {
            Integer target = graph.getEdgeTarget(edge);
            List<Integer> cycle = findCyclePath(graph, target, visited, path);
            if (cycle != null) {
                return cycle;
            }
        }
        
        // Backtrack
        path.remove(path.size() - 1);
        visited.remove(startVertex);
        
        return null;
    }
    
    @Override
    public List<CompetitorJob> reconcileData(List<CompetitorJob> jobsFromSource1,
                                            List<CompetitorJob> jobsFromSource2)
            throws DataAnomalyException {
        // JSON-first strategy: prefer JSON (source1) over XML (source2)
        Map<Integer, CompetitorJob> reconciledJobs = new HashMap<>();
        List<ContradictoryDataException> conflicts = new ArrayList<>();
        
        // First, add all jobs from source1 (JSON - preferred source)
        for (CompetitorJob job : jobsFromSource1) {
            if (job.getJobId() != null) {
                reconciledJobs.put(job.getJobId(), job);
            }
        }
        
        // Then, check source2 (XML) for conflicts and missing jobs
        for (CompetitorJob xmlJob : jobsFromSource2) {
            if (xmlJob.getJobId() == null) {
                continue;
            }
            
            CompetitorJob jsonJob = reconciledJobs.get(xmlJob.getJobId());
            
            if (jsonJob == null) {
                // Job exists only in XML - add it
                reconciledJobs.put(xmlJob.getJobId(), xmlJob);
            } else {
                // Job exists in both - check for conflicts
                String conflict = checkForConflicts(jsonJob, xmlJob);
                if (conflict != null) {
                    conflicts.add(new ContradictoryDataException(
                        "Contradictory data found for job",
                        xmlJob.getJobId(),
                        jsonJob.getSourceFile() != null ? jsonJob.getSourceFile() : "JSON",
                        xmlJob.getSourceFile() != null ? xmlJob.getSourceFile() : "XML",
                        conflict
                    ));
                }
            }
        }
        
        // If there are conflicts, throw exception with details
        if (!conflicts.isEmpty()) {
            StringBuilder message = new StringBuilder("Data reconciliation found contradictions: ");
            for (ContradictoryDataException conflict : conflicts) {
                message.append(conflict.getMessage()).append("; ");
            }
            throw new ContradictoryDataException(
                message.toString(),
                conflicts.get(0).getJobId(),
                conflicts.get(0).getSource1(),
                conflicts.get(0).getSource2(),
                conflicts.get(0).getConflictDescription()
            );
        }
        
        return new ArrayList<>(reconciledJobs.values());
    }
    
    /**
     * Checks for conflicts between two job definitions from different sources.
     * Returns conflict description if found, null otherwise.
     */
    private String checkForConflicts(CompetitorJob job1, CompetitorJob job2) {
        List<String> conflicts = new ArrayList<>();
        
        // Check job name
        if (!Objects.equals(job1.getJobName(), job2.getJobName())) {
            conflicts.add("Job name differs: '" + job1.getJobName() + "' vs '" + job2.getJobName() + "'");
        }
        
        // Check dependencies - this is the main conflict from Day 1 analysis
        Set<Integer> deps1 = extractDependencyIds(job1);
        Set<Integer> deps2 = extractDependencyIds(job2);
        
        if (!deps1.equals(deps2)) {
            conflicts.add("Dependencies differ: " + deps1 + " vs " + deps2);
        }
        
        return conflicts.isEmpty() ? null : String.join("; ", conflicts);
    }
    
    /**
     * Extracts dependency job IDs from a competitor job.
     */
    private Set<Integer> extractDependencyIds(CompetitorJob job) {
        Set<Integer> dependencyIds = new HashSet<>();
        if (job.getDependencies() != null) {
            for (JobDependency dep : job.getDependencies()) {
                if (dep.getDependentJobId() != null) {
                    dependencyIds.add(dep.getDependentJobId());
                }
            }
        }
        return dependencyIds;
    }
}

