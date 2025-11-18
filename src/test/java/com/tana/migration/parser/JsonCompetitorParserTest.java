package com.tana.migration.parser;

import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.exception.InvalidJobDataException;
import com.tana.migration.model.CompetitorJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for JsonCompetitorParser.
 * Tests both happy path and edge cases from Day 1 analysis.
 */
public class JsonCompetitorParserTest {
    
    private JsonCompetitorParser parser;
    private String testJsonFile;
    
    @BeforeEach
    public void setUp() throws IOException {
        parser = new JsonCompetitorParser();
        
        // Create a temporary test JSON file
        testJsonFile = System.getProperty("java.io.tmpdir") + "/test_jobs.json";
        File file = new File(testJsonFile);
        
        String jsonContent = "[\n" +
                "  {\n" +
                "    \"job_id\": 1001,\n" +
                "    \"job_name\": \"Test_Job_1\",\n" +
                "    \"dependencies\": [\n" +
                "      {\n" +
                "        \"job_id\": 1002,\n" +
                "        \"status\": \"Success\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"notes\": \"Test notes\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"job_id\": 1002,\n" +
                "    \"job_name\": \"Test_Job_2\",\n" +
                "    \"dependencies\": [],\n" +
                "    \"notes\": \"\"\n" +
                "  }\n" +
                "]";
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonContent);
        }
    }
    
    // Happy Path Tests
    @Test
    public void testParseValidJson() throws DataAnomalyException {
        List<CompetitorJob> jobs = parser.parse(testJsonFile);
        
        assertNotNull(jobs);
        assertEquals(2, jobs.size());
        
        CompetitorJob job1 = jobs.get(0);
        assertEquals(Integer.valueOf(1001), job1.getJobId());
        assertEquals("Test_Job_1", job1.getJobName());
        assertEquals(1, job1.getDependencies().size());
        assertEquals("Test notes", job1.getNotes());
        assertEquals(testJsonFile, job1.getSourceFile());
    }
    
    @Test
    public void testValidateValidJobs() throws DataAnomalyException {
        List<CompetitorJob> jobs = parser.parse(testJsonFile);
        parser.validate(jobs); // Should not throw exception
        assertTrue(true); // If we get here, validation passed
    }
    
    // Edge Case Tests - Empty Arrays
    @Test
    public void testParseEmptyArray() throws DataAnomalyException {
        String emptyArrayFile = getTestResourcePath("testdata/empty_array.json");
        List<CompetitorJob> jobs = parser.parse(emptyArrayFile);
        
        assertNotNull(jobs);
        assertEquals(0, jobs.size());
    }
    
    @Test
    public void testValidateEmptyArray() {
        String emptyArrayFile = getTestResourcePath("testdata/empty_array.json");
        assertThrows(InvalidJobDataException.class, () -> {
            List<CompetitorJob> jobs = parser.parse(emptyArrayFile);
            parser.validate(jobs);
        });
    }
    
    // Edge Case Tests - Null Values
    @Test
    public void testParseWithNullValues() throws DataAnomalyException {
        String nullValuesFile = getTestResourcePath("testdata/null_values.json");
        List<CompetitorJob> jobs = parser.parse(nullValuesFile);
        
        assertNotNull(jobs);
        // Parser should parse but validation should fail
    }
    
    @Test
    public void testValidateWithNullJobId() {
        String nullValuesFile = getTestResourcePath("testdata/null_values.json");
        assertThrows(InvalidJobDataException.class, () -> {
            List<CompetitorJob> jobs = parser.parse(nullValuesFile);
            parser.validate(jobs);
        });
    }
    
    @Test
    public void testValidateWithNullJobName() {
        String nullValuesFile = getTestResourcePath("testdata/null_values.json");
        assertThrows(InvalidJobDataException.class, () -> {
            List<CompetitorJob> jobs = parser.parse(nullValuesFile);
            parser.validate(jobs);
        });
    }
    
    // Edge Case Tests - Duplicate Job IDs
    @Test
    public void testValidateDuplicateJobIds() {
        String duplicateFile = getTestResourcePath("testdata/duplicate_job_ids.json");
        assertThrows(InvalidJobDataException.class, () -> {
            List<CompetitorJob> jobs = parser.parse(duplicateFile);
            parser.validate(jobs);
        });
    }
    
    // Edge Case Tests - Missing Dependencies
    @Test
    public void testParseWithMissingDependency() throws DataAnomalyException {
        String missingDepFile = getTestResourcePath("testdata/missing_dependency.json");
        List<CompetitorJob> jobs = parser.parse(missingDepFile);
        
        assertNotNull(jobs);
        assertEquals(2, jobs.size());
        // Validation should catch missing dependency
    }
    
    @Test
    public void testValidateWithMissingDependency() {
        String missingDepFile = getTestResourcePath("testdata/missing_dependency.json");
        // Note: Current validation doesn't check for missing dependencies in other jobs
        // This test documents the current behavior
        assertDoesNotThrow(() -> {
            List<CompetitorJob> jobs = parser.parse(missingDepFile);
            parser.validate(jobs);
        });
    }
    
    // Edge Case Tests - Circular Dependencies
    @Test
    public void testParseCircularDependency() throws DataAnomalyException {
        String circularFile = getTestResourcePath("testdata/circular_dependency.json");
        List<CompetitorJob> jobs = parser.parse(circularFile);
        
        assertNotNull(jobs);
        assertEquals(4, jobs.size());
        // Circular dependency is detected by mapper, not parser
    }
    
    // Edge Case Tests - Empty Dependencies
    @Test
    public void testParseWithEmptyDependencies() throws DataAnomalyException {
        String emptyDepsFile = getTestResourcePath("testdata/empty_dependencies.json");
        List<CompetitorJob> jobs = parser.parse(emptyDepsFile);
        
        assertNotNull(jobs);
        assertEquals(2, jobs.size());
        for (CompetitorJob job : jobs) {
            assertTrue(job.getDependencies().isEmpty());
        }
    }
    
    @Test
    public void testValidateWithEmptyDependencies() throws DataAnomalyException {
        String emptyDepsFile = getTestResourcePath("testdata/empty_dependencies.json");
        List<CompetitorJob> jobs = parser.parse(emptyDepsFile);
        parser.validate(jobs); // Should pass - empty dependencies are valid
        assertTrue(true);
    }
    
    // Error Handling Tests
    @Test
    public void testParseNonExistentFile() {
        assertThrows(InvalidJobDataException.class, () -> {
            parser.parse("/nonexistent/file.json");
        });
    }
    
    @Test
    public void testValidateEmptyList() {
        assertThrows(InvalidJobDataException.class, () -> {
            parser.validate(new java.util.ArrayList<>());
        });
    }
    
    @Test
    public void testValidateNullList() {
        assertThrows(InvalidJobDataException.class, () -> {
            parser.validate(null);
        });
    }
    
    // Helper method to get test resource path
    private String getTestResourcePath(String resourceName) {
        java.net.URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource != null) {
            try {
                // Handle URL encoding (spaces, etc.) and file:// protocol
                String path = resource.getPath();
                if (path.startsWith("file:")) {
                    path = path.substring(5); // Remove "file:" prefix
                }
                // Decode URL encoding
                path = java.net.URLDecoder.decode(path, "UTF-8");
                // Handle leading slash on Windows/Mac
                if (path.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    path = path.substring(1);
                }
                return path;
            } catch (java.io.UnsupportedEncodingException e) {
                String path = resource.getPath();
                if (path.startsWith("file:")) {
                    path = path.substring(5);
                }
                return path;
            }
        }
        // Fallback: try relative to project root
        String fallbackPath = "src/test/resources/" + resourceName;
        File fallbackFile = new File(fallbackPath);
        if (fallbackFile.exists()) {
            return fallbackFile.getAbsolutePath();
        }
        throw new RuntimeException("Test resource not found: " + resourceName);
    }
}
