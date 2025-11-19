package com.tana.migration.factory;

import com.tana.migration.CompetitorParser;
import com.tana.migration.parser.JsonCompetitorParser;
import com.tana.migration.parser.XmlCompetitorParser;

/**
 * Factory implementation that creates parsers based on file extension.
 * Follows Strategy Pattern - different parsers for different file types.
 * 
 * This implementation follows:
 * - Single Responsibility: Only responsible for parser creation based on file extension
 * - Open-Closed: Can be extended with new file types without modifying existing code
 * - Dependency Inversion: Returns CompetitorParser interface, not concrete classes
 */
public class FileExtensionParserFactory implements ParserFactory {
    
    @Override
    public CompetitorParser createParser(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        
        String lowerPath = filePath.toLowerCase();
        
        if (lowerPath.endsWith(".json")) {
            return new JsonCompetitorParser();
        } else if (lowerPath.endsWith(".xml")) {
            return new XmlCompetitorParser();
        } else {
            // Default to JSON parser for unknown extensions
            return new JsonCompetitorParser();
        }
    }
}

