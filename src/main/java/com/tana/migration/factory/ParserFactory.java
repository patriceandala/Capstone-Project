package com.tana.migration.factory;

import com.tana.migration.CompetitorParser;

/**
 * Factory interface for creating parser instances.
 * Follows Factory Pattern and Dependency Inversion Principle.
 * 
 * High-level modules depend on this abstraction rather than concrete parser implementations.
 */
public interface ParserFactory {
    
    /**
     * Creates an appropriate parser for the given file path.
     * 
     * @param filePath Path to the file to be parsed
     * @return Appropriate CompetitorParser instance for the file type
     */
    CompetitorParser createParser(String filePath);
}

