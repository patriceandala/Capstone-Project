package com.tana.migration.parser;

import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.exception.InvalidJobDataException;
import com.tana.migration.model.CompetitorJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for XmlCompetitorParser.
 * Tests both happy path and edge cases.
 */
public class XmlCompetitorParserTest {
    
    private XmlCompetitorParser parser;
    
    @BeforeEach
    public void setUp() {
        parser = new XmlCompetitorParser();
    }
    
    // Happy Path Tests
    @Test
    public void testParseValidXml() throws DataAnomalyException {
        String xmlFile = getTestResourcePath("testdata/contradictory_xml.xml");
        List<CompetitorJob> jobs = parser.parse(xmlFile);
        
        assertNotNull(jobs);
        assertEquals(4, jobs.size());
        
        CompetitorJob job1 = jobs.get(0);
        assertEquals(Integer.valueOf(2001), job1.getJobId());
        assertEquals("Backup_Production_Databases_Oracle", job1.getJobName());
    }
    
    @Test
    public void testParseXmlWithDependencies() throws DataAnomalyException {
        String xmlFile = getTestResourcePath("testdata/contradictory_xml.xml");
        List<CompetitorJob> jobs = parser.parse(xmlFile);
        
        // Job 3100 should have dependency on 2001
        CompetitorJob job3100 = jobs.stream()
                .filter(j -> j.getJobId().equals(3100))
                .findFirst()
                .orElse(null);
        
        assertNotNull(job3100);
        assertEquals(1, job3100.getDependencies().size());
        assertEquals(Integer.valueOf(2001), job3100.getDependencies().get(0).getDependentJobId());
    }
    
    // Edge Case Tests - Empty XML
    @Test
    public void testParseEmptyXml() throws DataAnomalyException {
        String emptyXmlFile = getTestResourcePath("testdata/empty_xml.xml");
        List<CompetitorJob> jobs = parser.parse(emptyXmlFile);
        
        assertNotNull(jobs);
        assertEquals(0, jobs.size());
    }
    
    @Test
    public void testValidateEmptyXml() {
        String emptyXmlFile = getTestResourcePath("testdata/empty_xml.xml");
        assertThrows(InvalidJobDataException.class, () -> {
            List<CompetitorJob> jobs = parser.parse(emptyXmlFile);
            parser.validate(jobs);
        });
    }
    
    // Edge Case Tests - Missing Dependencies
    @Test
    public void testParseXmlWithMissingDependency() throws DataAnomalyException {
        String missingDepFile = getTestResourcePath("testdata/missing_dependency_xml.xml");
        List<CompetitorJob> jobs = parser.parse(missingDepFile);
        
        assertNotNull(jobs);
        assertEquals(2, jobs.size());
        
        // Job 1001 should have dependency on 9999 (which doesn't exist)
        CompetitorJob job1001 = jobs.stream()
                .filter(j -> j.getJobId().equals(1001))
                .findFirst()
                .orElse(null);
        
        assertNotNull(job1001);
        assertEquals(1, job1001.getDependencies().size());
        assertEquals(Integer.valueOf(9999), job1001.getDependencies().get(0).getDependentJobId());
    }
    
    // Error Handling Tests
    @Test
    public void testParseNonExistentFile() {
        assertThrows(InvalidJobDataException.class, () -> {
            parser.parse("/nonexistent/file.xml");
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
        java.io.File fallbackFile = new java.io.File(fallbackPath);
        if (fallbackFile.exists()) {
            return fallbackFile.getAbsolutePath();
        }
        throw new RuntimeException("Test resource not found: " + resourceName);
    }
}

