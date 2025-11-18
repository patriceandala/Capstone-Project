package com.tana.migration.parser;

import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.model.CompetitorJob;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for JsonCompetitorParser.
 */
public class JsonCompetitorParserTest {
    
    private JsonCompetitorParser parser;
    private String testJsonFile;
    
    @Before
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
    }
    
    @Test(expected = DataAnomalyException.class)
    public void testParseNonExistentFile() throws DataAnomalyException {
        parser.parse("/nonexistent/file.json");
    }
    
    @Test(expected = DataAnomalyException.class)
    public void testValidateEmptyList() throws DataAnomalyException {
        parser.validate(new java.util.ArrayList<>());
    }
}

