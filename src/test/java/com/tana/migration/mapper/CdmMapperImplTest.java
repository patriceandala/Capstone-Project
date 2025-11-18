package com.tana.migration.mapper;

import com.tana.migration.exception.CircularDependencyException;
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
 * Unit tests for CdmMapperImpl.
 */
public class CdmMapperImplTest {
    
    private CdmMapperImpl mapper;
    
    @BeforeEach
    public void setUp() {
        mapper = new CdmMapperImpl();
    }
    
    @Test
    public void testMapToRmj_ValidJobs() throws DataAnomalyException {
        List<CompetitorJob> competitorJobs = createValidJobs();
        List<RmjJob> rmjJobs = mapper.mapToRmj(competitorJobs);
        
        assertNotNull(rmjJobs);
        assertEquals(2, rmjJobs.size());
        
        RmjJob job1 = rmjJobs.get(0);
        assertEquals(Integer.valueOf(1001), job1.getJobId());
        assertEquals("Test_Job_1", job1.getJobName());
        assertEquals(1, job1.getDependencyJobIds().size());
        assertEquals(Integer.valueOf(1002), job1.getDependencyJobIds().get(0));
    }
    
    @Test
    public void testMapToRmj_CircularDependency() {
        List<CompetitorJob> jobs = createCircularDependencyJobs();
        
        assertThrows(CircularDependencyException.class, () -> {
            mapper.mapToRmj(jobs);
        });
    }
    
    @Test
    public void testDetectCircularDependencies_NoCycle() {
        List<CompetitorJob> jobs = createValidJobs();
        List<List<Integer>> cycles = mapper.detectCircularDependencies(jobs);
        
        assertTrue(cycles.isEmpty());
    }
    
    @Test
    public void testDetectCircularDependencies_WithCycle() {
        List<CompetitorJob> jobs = createCircularDependencyJobs();
        List<List<Integer>> cycles = mapper.detectCircularDependencies(jobs);
        
        assertFalse(cycles.isEmpty());
        // Should detect the cycle
        assertTrue(cycles.size() > 0);
    }
    
    @Test
    public void testReconcileData_NoConflicts() throws DataAnomalyException {
        List<CompetitorJob> source1 = createValidJobs();
        List<CompetitorJob> source2 = createValidJobs();
        
        List<CompetitorJob> reconciled = mapper.reconcileData(source1, source2);
        
        assertNotNull(reconciled);
        assertEquals(2, reconciled.size());
    }
    
    @Test
    public void testReconcileData_WithConflicts() {
        List<CompetitorJob> source1 = createValidJobs();
        List<CompetitorJob> source2 = createConflictingJobs();
        
        assertThrows(DataAnomalyException.class, () -> {
            mapper.reconcileData(source1, source2);
        });
    }
    
    // Helper methods to create test data
    private List<CompetitorJob> createValidJobs() {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job1 = new CompetitorJob(1001, "Test_Job_1");
        job1.addDependency(new JobDependency(1002));
        jobs.add(job1);
        
        CompetitorJob job2 = new CompetitorJob(1002, "Test_Job_2");
        jobs.add(job2);
        
        return jobs;
    }
    
    private List<CompetitorJob> createCircularDependencyJobs() {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job1 = new CompetitorJob(1001, "Job_1");
        job1.addDependency(new JobDependency(1002));
        jobs.add(job1);
        
        CompetitorJob job2 = new CompetitorJob(1002, "Job_2");
        job2.addDependency(new JobDependency(1001)); // Creates cycle
        jobs.add(job2);
        
        return jobs;
    }
    
    private List<CompetitorJob> createConflictingJobs() {
        List<CompetitorJob> jobs = new ArrayList<>();
        
        CompetitorJob job1 = new CompetitorJob(1001, "Different_Name");
        job1.addDependency(new JobDependency(1003)); // Different dependency
        job1.setSourceFile("source2.json");
        jobs.add(job1);
        
        return jobs;
    }
}
