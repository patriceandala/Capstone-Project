package com.tana.migration.service;

import com.tana.migration.CompetitorParser;
import com.tana.migration.exception.DataAnomalyException;
import com.tana.migration.model.CompetitorJob;
import com.tana.migration.parser.JsonCompetitorParser;
import com.tana.migration.parser.XmlCompetitorParser;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service for parsing multiple competitor data files concurrently.
 * Uses ExecutorService for thread pool management and better control over concurrency.
 * 
 * Thread Safety:
 * - Each parsing task is independent (no shared mutable state during parsing)
 * - Results are collected using thread-safe Collections.synchronizedList
 * - Exceptions are collected in a thread-safe ConcurrentLinkedQueue
 * - Final aggregation uses thread-safe operations
 */
public class ConcurrentFileParserService {
    
    private final ExecutorService executorService;
    private final int threadPoolSize;
    
    /**
     * Creates a ConcurrentFileParserService with default thread pool size
     * (number of available processors).
     */
    public ConcurrentFileParserService() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Creates a ConcurrentFileParserService with specified thread pool size.
     * 
     * @param threadPoolSize Number of threads in the pool
     */
    public ConcurrentFileParserService(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    /**
     * Parses multiple files concurrently and returns all parsed jobs.
     * 
     * @param filePaths List of file paths to parse (can be JSON or XML)
     * @return ParsingResult containing all parsed jobs and any parsing errors
     */
    public ParsingResult parseFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return new ParsingResult(Collections.emptyList(), Collections.emptyList());
        }
        
        // Thread-safe collections for results
        List<CompetitorJob> allJobs = Collections.synchronizedList(new ArrayList<>());
        Queue<FileParsingError> errors = new ConcurrentLinkedQueue<>();
        
        // Create parsing tasks
        List<Future<ParsingTaskResult>> futures = new ArrayList<>();
        
        for (String filePath : filePaths) {
            Future<ParsingTaskResult> future = executorService.submit(() -> parseFile(filePath));
            futures.add(future);
        }
        
        // Collect results from all tasks
        for (Future<ParsingTaskResult> future : futures) {
            try {
                ParsingTaskResult result = future.get(); // Wait for completion
                if (result.getJobs() != null) {
                    allJobs.addAll(result.getJobs());
                }
                if (result.getError() != null) {
                    errors.add(result.getError());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                errors.add(new FileParsingError("Unknown", "Parsing interrupted: " + e.getMessage()));
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                errors.add(new FileParsingError("Unknown", 
                    "Execution error: " + (cause != null ? cause.getMessage() : e.getMessage())));
            }
        }
        
        return new ParsingResult(new ArrayList<>(allJobs), new ArrayList<>(errors));
    }
    
    /**
     * Parses a single file. This method is called by each thread.
     * 
     * @param filePath Path to the file to parse
     * @return ParsingTaskResult with jobs and any error
     */
    private ParsingTaskResult parseFile(String filePath) {
        try {
            // Determine parser based on file extension
            CompetitorParser parser = getParserForFile(filePath);
            
            // Parse the file
            List<CompetitorJob> jobs = parser.parse(filePath);
            
            // Validate parsed jobs
            parser.validate(jobs);
            
            return new ParsingTaskResult(jobs, null);
            
        } catch (DataAnomalyException e) {
            return new ParsingTaskResult(null, 
                new FileParsingError(filePath, "Data anomaly: " + e.getMessage()));
        } catch (Exception e) {
            return new ParsingTaskResult(null, 
                new FileParsingError(filePath, "Unexpected error: " + e.getMessage()));
        }
    }
    
    /**
     * Determines the appropriate parser based on file extension.
     * 
     * @param filePath Path to the file
     * @return Appropriate parser (JsonCompetitorParser or XmlCompetitorParser)
     */
    private CompetitorParser getParserForFile(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".json")) {
            return new JsonCompetitorParser();
        } else if (lowerPath.endsWith(".xml")) {
            return new XmlCompetitorParser();
        } else {
            // Default to JSON parser
            return new JsonCompetitorParser();
        }
    }
    
    /**
     * Shuts down the executor service. Should be called when done with the service.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Result of parsing multiple files.
     */
    public static class ParsingResult {
        private final List<CompetitorJob> jobs;
        private final List<FileParsingError> errors;
        
        public ParsingResult(List<CompetitorJob> jobs, List<FileParsingError> errors) {
            this.jobs = jobs != null ? jobs : Collections.emptyList();
            this.errors = errors != null ? errors : Collections.emptyList();
        }
        
        public List<CompetitorJob> getJobs() {
            return jobs;
        }
        
        public List<FileParsingError> getErrors() {
            return errors;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public int getTotalJobsParsed() {
            return jobs.size();
        }
    }
    
    /**
     * Result of a single file parsing task.
     */
    private static class ParsingTaskResult {
        private final List<CompetitorJob> jobs;
        private final FileParsingError error;
        
        public ParsingTaskResult(List<CompetitorJob> jobs, FileParsingError error) {
            this.jobs = jobs;
            this.error = error;
        }
        
        public List<CompetitorJob> getJobs() {
            return jobs;
        }
        
        public FileParsingError getError() {
            return error;
        }
    }
    
    /**
     * Represents an error that occurred during file parsing.
     */
    public static class FileParsingError {
        private final String filePath;
        private final String errorMessage;
        
        public FileParsingError(String filePath, String errorMessage) {
            this.filePath = filePath;
            this.errorMessage = errorMessage;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public String toString() {
            return String.format("File: %s, Error: %s", filePath, errorMessage);
        }
    }
}

