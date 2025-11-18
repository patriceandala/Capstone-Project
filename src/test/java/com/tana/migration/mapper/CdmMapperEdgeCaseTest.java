package com.tana.migration.mapper;

import com.tana.migration.exception.CircularDependencyException;
import com.tana.migration.exception.ContradictoryDataException;
import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.model.CompetitorJob;
import com.tana.migration.model.JobDependency;
import com.tana.migration.model.RmjJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive edge case tests for CdmMapper.
 * Tests scenarios identified in Day 1 analysis.
 */
public class CdmMapperEdgeCaseTest {
    
    private CdmMapperImpl mapper;
    
    @BeforeEach
    public void setUp() {
        mapper = new CdmMapperImpl();
    }
    
    // Edge Case: Empty Input
    @Test
    public void testMapToRmj_EmptyList() throws DataAnomalyException {
        List<CompetitorJob> emptyList = new ArrayList<>();
        List<RmjJob> result = mapper.mapToRmj(emptyList);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testMapToRmj_NullList() throws DataAnomalyException {
        List<RmjJob> result = mapper.mapToRmj(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    // Edge Case: Jobs with No Dependencies
    @Test
    public void testMapToRmj_JobsWithNoDependencies() throws DataAnomalyException {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job1 = new CompetitorJob(1001, "Job_No_Deps");
        jobs.add(job1);
        
        CompetitorJob job2 = new CompetitorJob(1002, "Another_Job_No_Deps");
        jobs.add(job2);
        
        List<RmjJob> rmjJobs = mapper.mapToRmj(jobs);
        
        assertEquals(2, rmjJobs.size());
        assertTrue(rmjJobs.get(0).getDependencyJobIds().isEmpty());
        assertTrue(rmjJobs.get(1).getDependencyJobIds().isEmpty());
    }
    
    // Edge Case: Jobs with Null Dependencies List
    @Test
    public void testMapToRmj_JobsWithNullDependencies() throws DataAnomalyException {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job = new CompetitorJob(1001, "Job_With_Null_Deps");
        job.setDependencies(null); // Explicitly set to null
        jobs.add(job);
        
        List<RmjJob> rmjJobs = mapper.mapToRmj(jobs);
        
        assertEquals(1, rmjJobs.size());
        assertTrue(rmjJobs.get(0).getDependencyJobIds().isEmpty());
    }
    
    // Edge Case: Dependencies with Null Job IDs
    @Test
    public void testMapToRmj_DependenciesWithNullJobIds() throws DataAnomalyException {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job = new CompetitorJob(1001, "Job_With_Null_Dep_ID");
        JobDependency dep = new JobDependency(null); // Null dependency job ID
        job.addDependency(dep);
        jobs.add(job);
        
        List<RmjJob> rmjJobs = mapper.mapToRmj(jobs);
        
        assertEquals(1, rmjJobs.size());
        assertTrue(rmjJobs.get(0).getDependencyJobIds().isEmpty()); // Null dependencies should be skipped
    }
    
    // Edge Case: Circular Dependency - Simple 2-Node Cycle
    @Test
    public void testMapToRmj_SimpleCircularDependency() {
        List<CompetitorJob> jobs = createSimpleCycle();
        
        assertThrows(CircularDependencyException.class, () -> {
            mapper.mapToRmj(jobs);
        });
    }
    
    // Edge Case: Circular Dependency - Complex 4-Node Cycle (Day 1 scenario)
    @Test
    public void testMapToRmj_ComplexCircularDependency() {
        List<CompetitorJob> jobs = createComplexCycle();
        
        CircularDependencyException exception = assertThrows(CircularDependencyException.class, () -> {
            mapper.mapToRmj(jobs);
        });
        
        assertNotNull(exception.getCyclePath());
        assertFalse(exception.getCyclePath().isEmpty());
    }
    
    // Edge Case: Multiple Circular Dependencies
    @Test
    public void testDetectCircularDependencies_MultipleCycles() {
        List<CompetitorJob> jobs = createMultipleCycles();
        
        List<List<Integer>> cycles = mapper.detectCircularDependencies(jobs);
        
        assertFalse(cycles.isEmpty());
        // Should detect at least one cycle
    }
    
    // Edge Case: Self-Referencing Dependency (job depends on itself)
    @Test
    public void testMapToRmj_SelfReferencingDependency() {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job = new CompetitorJob(1001, "Self_Referencing_Job");
        job.addDependency(new JobDependency(1001)); // Depends on itself
        jobs.add(job);
        
        assertThrows(CircularDependencyException.class, () -> {
            mapper.mapToRmj(jobs);
        });
    }
    
    // Edge Case: Contradictory Data - Different Job Names
    @Test
    public void testReconcileData_DifferentJobNames() {
        List<CompetitorJob> source1 = new ArrayList<>();
        CompetitorJob job1 = new CompetitorJob(1001, "Job_Name_From_JSON");
        job1.setSourceFile("source1.json");
        source1.add(job1);
        
        List<CompetitorJob> source2 = new ArrayList<>();
        CompetitorJob job2 = new CompetitorJob(1001, "Different_Name_From_XML");
        job2.setSourceFile("source2.xml");
        source2.add(job2);
        
        assertThrows(ContradictoryDataException.class, () -> {
            mapper.reconcileData(source1, source2);
        });
    }
    
    // Edge Case: Contradictory Data - Different Dependencies (Day 1 scenario)
    @Test
    public void testReconcileData_DifferentDependencies() {
        List<CompetitorJob> source1 = new ArrayList<>();
        CompetitorJob job1 = new CompetitorJob(4100, "Consolidate_Financials_SAP");
        job1.addDependency(new JobDependency(3200));
        job1.setSourceFile("export_A.json");
        source1.add(job1);
        
        List<CompetitorJob> source2 = new ArrayList<>();
        CompetitorJob job2 = new CompetitorJob(4100, "Consolidate_Financials_SAP");
        job2.addDependency(new JobDependency(2001)); // Different dependency
        job2.setSourceFile("export_B.xml");
        source2.add(job2);
        
        ContradictoryDataException exception = assertThrows(ContradictoryDataException.class, () -> {
            mapper.reconcileData(source1, source2);
        });
        
        assertEquals(Integer.valueOf(4100), exception.getJobId());
        assertTrue(exception.getConflictDescription().contains("Dependencies differ"));
    }
    
    // Edge Case: Jobs Only in One Source
    @Test
    public void testReconcileData_JobsOnlyInOneSource() throws DataAnomalyException {
        List<CompetitorJob> source1 = new ArrayList<>();
        CompetitorJob job1 = new CompetitorJob(1001, "Job_Only_In_JSON");
        job1.setSourceFile("source1.json");
        source1.add(job1);
        
        List<CompetitorJob> source2 = new ArrayList<>();
        CompetitorJob job2 = new CompetitorJob(1002, "Job_Only_In_XML");
        job2.setSourceFile("source2.xml");
        source2.add(job2);
        
        List<CompetitorJob> reconciled = mapper.reconcileData(source1, source2);
        
        assertEquals(2, reconciled.size());
        // Both jobs should be included
    }
    
    // Edge Case: Large Number of Jobs
    @Test
    public void testMapToRmj_LargeNumberOfJobs() throws DataAnomalyException {
        List<CompetitorJob> jobs = createLargeJobList(100);
        
        List<RmjJob> rmjJobs = mapper.mapToRmj(jobs);
        
        assertEquals(100, rmjJobs.size());
    }
    
    // Edge Case: Jobs with Many Dependencies
    @Test
    public void testMapToRmj_JobWithManyDependencies() throws DataAnomalyException {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        // Create a job with 10 dependencies
        CompetitorJob job = new CompetitorJob(1001, "Job_With_Many_Deps");
        for (int i = 1002; i <= 1011; i++) {
            job.addDependency(new JobDependency(i));
            CompetitorJob depJob = new CompetitorJob(i, "Dependency_Job_" + i);
            jobs.add(depJob);
        }
        jobs.add(job);
        
        List<RmjJob> rmjJobs = mapper.mapToRmj(jobs);
        
        RmjJob mappedJob = rmjJobs.stream()
                .filter(j -> j.getJobId().equals(1001))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mappedJob);
        assertEquals(10, mappedJob.getDependencyJobIds().size());
    }
    
    // Helper Methods
    private List<CompetitorJob> createSimpleCycle() {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job1 = new CompetitorJob(1001, "Job_1");
        job1.addDependency(new JobDependency(1002));
        jobs.add(job1);
        
        CompetitorJob job2 = new CompetitorJob(1002, "Job_2");
        job2.addDependency(new JobDependency(1001)); // Creates cycle
        jobs.add(job2);
        
        return jobs;
    }
    
    private List<CompetitorJob> createComplexCycle() {
        // Day 1 scenario: 2001 → 4100 → 3200 → 3100 → 2001
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job2001 = new CompetitorJob(2001, "Job_2001");
        job2001.addDependency(new JobDependency(4100));
        jobs.add(job2001);
        
        CompetitorJob job3100 = new CompetitorJob(3100, "Job_3100");
        job3100.addDependency(new JobDependency(2001));
        jobs.add(job3100);
        
        CompetitorJob job3200 = new CompetitorJob(3200, "Job_3200");
        job3200.addDependency(new JobDependency(3100));
        jobs.add(job3200);
        
        CompetitorJob job4100 = new CompetitorJob(4100, "Job_4100");
        job4100.addDependency(new JobDependency(3200));
        jobs.add(job4100);
        
        return jobs;
    }
    
    private List<CompetitorJob> createMultipleCycles() {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        // Cycle 1: 1001 → 1002 → 1001
        CompetitorJob job1 = new CompetitorJob(1001, "Job_1");
        job1.addDependency(new JobDependency(1002));
        jobs.add(job1);
        
        CompetitorJob job2 = new CompetitorJob(1002, "Job_2");
        job2.addDependency(new JobDependency(1001));
        jobs.add(job2);
        
        // Cycle 2: 2001 → 2002 → 2001
        CompetitorJob job3 = new CompetitorJob(2001, "Job_3");
        job3.addDependency(new JobDependency(2002));
        jobs.add(job3);
        
        CompetitorJob job4 = new CompetitorJob(2002, "Job_4");
        job4.addDependency(new JobDependency(2001));
        jobs.add(job4);
        
        return jobs;
    }
    
    private List<CompetitorJob> createLargeJobList(int count) {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            CompetitorJob job = new CompetitorJob(i, "Job_" + i);
            if (i > 1) {
                job.addDependency(new JobDependency(i - 1)); // Each job depends on previous
            }
            jobs.add(job);
        }
        
        return jobs;
    }
}

