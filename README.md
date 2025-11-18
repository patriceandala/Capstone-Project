# Capstone Phase 3 - Competitor Data Migration

## Overview
This project implements the parsing and mapping logic for migrating competitor workflow data to Redwood Migration Job (RMJ) format, based on the analysis completed in Day 1.

## Project Structure

```
src/main/java/com/tana/migration/
├── CompetitorParser.java          # Interface for parsing competitor data files
├── CdmMapper.java                 # Interface for mapping to RMJ format
├── model/
│   ├── CompetitorJob.java         # Model for competitor job data
│   ├── JobDependency.java         # Model for job dependencies
│   └── RmjJob.java                # Model for RMJ format
└── exception/
    ├── DataAnomalyException.java  # Base exception for data anomalies
    ├── CircularDependencyException.java
    ├── ContradictoryDataException.java
    └── InvalidJobDataException.java
```

## Key Features

### 1. CompetitorParser Interface
- Parses JSON and XML competitor data files
- Validates parsed data
- Handles format inconsistencies identified in Day 1 analysis

### 2. CdmMapper Interface
- Maps competitor jobs to RMJ format
- Detects circular dependencies using graph algorithms
- Reconciles contradictory data between sources (JSON-first strategy)

### 3. Custom Exceptions
Based on Day 1 analysis, custom exceptions handle:
- **CircularDependencyException**: For cycles like 2001 → 4100 → 3200 → 3100 → 2001
- **ContradictoryDataException**: For conflicts like Job 4100 having different dependencies in JSON vs XML
- **InvalidJobDataException**: For missing fields, invalid formats, etc.

## Design Principles

- **SOLID Principles**: 
  - Single Responsibility: Parser handles parsing, Mapper handles mapping
  - Open-Closed: Interfaces allow extension without modification
  - Dependency Inversion: Depend on abstractions (interfaces)

- **Java Collections**: Appropriate use of Lists, Maps, Sets for data structures

## Dependencies

- Jackson 2.15.2 (JSON/XML parsing)
- JGraphT 1.5.2 (Graph analysis and cycle detection)
- JUnit 4.13.2 (Testing)
- Mockito 3.12.4 (Testing)

## Next Steps

1. Implement concrete classes for `CompetitorParser` (JSON and XML parsers)
2. Implement `CdmMapper` with cycle detection using JGraphT
3. Add unit tests
4. Push to GitHub

