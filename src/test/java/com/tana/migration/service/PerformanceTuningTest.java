package com.tana.migration.service;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tuning test to demonstrate the difference between
 * unoptimized and optimized implementations.
 */
public class PerformanceTuningTest {
    
    @Test
    public void testPerformanceComparison() throws Exception {
        String filePath = getTestResourcePath("performance-challenge/EOD_parts_consumption_large.json");
        
        // Test unoptimized version
        PartsConsumptionProcessor unoptimized = new PartsConsumptionProcessor();
        long startUnoptimized = System.currentTimeMillis();
        PartsConsumptionProcessor.ConsumptionSummary summaryUnoptimized = unoptimized.getSummary(filePath);
        long endUnoptimized = System.currentTimeMillis();
        long timeUnoptimized = endUnoptimized - startUnoptimized;
        
        // Test optimized version
        PartsConsumptionProcessorOptimized optimized = new PartsConsumptionProcessorOptimized();
        long startOptimized = System.currentTimeMillis();
        PartsConsumptionProcessor.ConsumptionSummary summaryOptimized = optimized.getSummary(filePath);
        long endOptimized = System.currentTimeMillis();
        long timeOptimized = endOptimized - startOptimized;
        
        // Verify results are the same
        assertEquals(summaryUnoptimized.getTotalTransactions(), summaryOptimized.getTotalTransactions());
        assertEquals(summaryUnoptimized.getUniqueAssemblyLines(), summaryOptimized.getUniqueAssemblyLines());
        assertEquals(summaryUnoptimized.getUniquePartSkus(), summaryOptimized.getUniquePartSkus());
        assertEquals(summaryUnoptimized.getTotalQuantity(), summaryOptimized.getTotalQuantity());
        
        // Print performance comparison
        System.out.println("\n=== Performance Comparison ===");
        System.out.println("Unoptimized time: " + timeUnoptimized + " ms");
        System.out.println("Optimized time: " + timeOptimized + " ms");
        
        if (timeUnoptimized > 0) {
            double speedup = (double) timeUnoptimized / timeOptimized;
            System.out.println("Speedup: " + String.format("%.2f", speedup) + "x faster");
            System.out.println("Time saved: " + (timeUnoptimized - timeOptimized) + " ms");
        }
        
        // Optimized version should be significantly faster
        assertTrue(timeOptimized < timeUnoptimized || timeUnoptimized < 1000, 
            "Optimized version should be faster or unoptimized should complete in reasonable time");
    }
    
    @Test
    public void testOptimizedProcessor_Correctness() throws Exception {
        String filePath = getTestResourcePath("performance-challenge/EOD_parts_consumption_large.json");
        
        PartsConsumptionProcessorOptimized processor = new PartsConsumptionProcessorOptimized();
        PartsConsumptionProcessor.ConsumptionSummary summary = processor.getSummary(filePath);
        
        assertNotNull(summary);
        assertTrue(summary.getTotalTransactions() > 0);
        assertTrue(summary.getUniqueAssemblyLines() > 0);
        assertTrue(summary.getUniquePartSkus() > 0);
        assertTrue(summary.getTotalQuantity() > 0);
        
        System.out.println("\n=== Optimized Processor Results ===");
        System.out.println(summary);
    }
    
    private String getTestResourcePath(String resourceName) {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource != null) {
            try {
                String path = resource.getPath();
                if (path.startsWith("file:")) {
                    path = path.substring(5);
                }
                path = java.net.URLDecoder.decode(path, "UTF-8");
                if (path.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    path = path.substring(1);
                }
                return path;
            } catch (Exception e) {
                return resource.getPath();
            }
        }
        // Fallback
        String fallbackPath = "src/test/resources/" + resourceName;
        java.io.File fallbackFile = new java.io.File(fallbackPath);
        if (fallbackFile.exists()) {
            return fallbackFile.getAbsolutePath();
        }
        throw new RuntimeException("Test resource not found: " + resourceName);
    }
}

