package com.tana.migration.service;

import com.tana.migration.model.CompetitorJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConcurrentFileParserService.
 * Tests concurrent parsing, thread safety, and error handling.
 */
public class ConcurrentFileParserServiceTest {
    
    private ConcurrentFileParserService service;
    private List<String> tempFiles;
    
    @BeforeEach
    public void setUp() throws IOException {
        service = new ConcurrentFileParserService(4); // Use 4 threads for testing
        tempFiles = new ArrayList<>();
        
        // Create temporary test files
        createTempJsonFile("test1.json", 1001, "Job_1");
        createTempJsonFile("test2.json", 1002, "Job_2");
        createTempXmlFile("test3.xml", 1003, "Job_3");
    }
    
    @AfterEach
    public void tearDown() {
        service.shutdown();
        // Clean up temp files
        for (String filePath : tempFiles) {
            new File(filePath).delete();
        }
    }
    
    @Test
    public void testParseFiles_SingleFile() {
        List<String> filePaths = Arrays.asList(tempFiles.get(0));
        
        ConcurrentFileParserService.ParsingResult result = service.parseFiles(filePaths);
        
        assertNotNull(result);
        assertFalse(result.hasErrors());
        assertEquals(1, result.getTotalJobsParsed());
    }
    
    @Test
    public void testParseFiles_MultipleFiles() {
        ConcurrentFileParserService.ParsingResult result = service.parseFiles(tempFiles);
        
        assertNotNull(result);
        assertFalse(result.hasErrors());
        assertEquals(3, result.getTotalJobsParsed());
    }
    
    @Test
    public void testParseFiles_MixedJsonAndXml() {
        ConcurrentFileParserService.ParsingResult result = service.parseFiles(tempFiles);
        
        assertNotNull(result);
        assertFalse(result.hasErrors());
        assertEquals(3, result.getTotalJobsParsed());
        
        // Verify all jobs were parsed
        List<CompetitorJob> jobs = result.getJobs();
        assertEquals(3, jobs.size());
    }
    
    @Test
    public void testParseFiles_WithInvalidFile() {
        List<String> filePaths = new ArrayList<>(tempFiles);
        filePaths.add("/nonexistent/file.json");
        
        ConcurrentFileParserService.ParsingResult result = service.parseFiles(filePaths);
        
        assertNotNull(result);
        assertTrue(result.hasErrors());
        assertEquals(3, result.getTotalJobsParsed()); // Valid files still parsed
        assertEquals(1, result.getErrors().size()); // One error for invalid file
    }
    
    @Test
    public void testParseFiles_EmptyList() {
        ConcurrentFileParserService.ParsingResult result = service.parseFiles(new ArrayList<>());
        
        assertNotNull(result);
        assertFalse(result.hasErrors());
        assertEquals(0, result.getTotalJobsParsed());
    }
    
    @Test
    public void testParseFiles_NullList() {
        ConcurrentFileParserService.ParsingResult result = service.parseFiles(null);
        
        assertNotNull(result);
        assertFalse(result.hasErrors());
        assertEquals(0, result.getTotalJobsParsed());
    }
    
    @Test
    public void testParseFiles_ThreadSafety() {
        // Create many files to test concurrent parsing
        List<String> manyFiles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            try {
                String filePath = createTempJsonFile("concurrent_test_" + i + ".json", 2000 + i, "Concurrent_Job_" + i);
                manyFiles.add(filePath);
            } catch (IOException e) {
                fail("Failed to create test file: " + e.getMessage());
            }
        }
        
        ConcurrentFileParserService.ParsingResult result = service.parseFiles(manyFiles);
        
        assertNotNull(result);
        assertFalse(result.hasErrors());
        assertEquals(20, result.getTotalJobsParsed());
        
        // Verify no duplicate jobs (thread safety check)
        List<Integer> jobIds = new ArrayList<>();
        for (CompetitorJob job : result.getJobs()) {
            assertFalse(jobIds.contains(job.getJobId()), 
                "Duplicate job ID found: " + job.getJobId());
            jobIds.add(job.getJobId());
        }
    }
    
    // Helper methods
    private String createTempJsonFile(String fileName, int jobId, String jobName) throws IOException {
        String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
        File file = new File(filePath);
        
        String jsonContent = String.format(
            "[{\"job_id\": %d, \"job_name\": \"%s\", \"dependencies\": [], \"notes\": \"\"}]",
            jobId, jobName
        );
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonContent);
        }
        
        tempFiles.add(filePath);
        return filePath;
    }
    
    private String createTempXmlFile(String fileName, int jobId, String jobName) throws IOException {
        String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
        File file = new File(filePath);
        
        String xmlContent = String.format(
            "<?xml version=\"1.0\"?><ProcessChain><Job name=\"%s\" id=\"%d\"/></ProcessChain>",
            jobName, jobId
        );
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(xmlContent);
        }
        
        tempFiles.add(filePath);
        return filePath;
    }
}

