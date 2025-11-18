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

## Implementation Status

✅ **Completed:**
1. Implemented `JsonCompetitorParser` and `XmlCompetitorParser` 
2. Implemented `CdmMapperImpl` with JGraphT cycle detection
3. Added unit tests (10 tests, all passing)
4. Applied SOLID principles throughout

## Usage Example

```java
// Parse JSON file
CompetitorParser jsonParser = new JsonCompetitorParser();
List<CompetitorJob> jsonJobs = jsonParser.parse("export_A_with_circular_dependency.json");
jsonParser.validate(jsonJobs);

// Parse XML file
CompetitorParser xmlParser = new XmlCompetitorParser();
List<CompetitorJob> xmlJobs = xmlParser.parse("export_B_contradictory.xml");
xmlParser.validate(xmlJobs);

// Reconcile data
CdmMapper mapper = new CdmMapperImpl();
List<CompetitorJob> reconciledJobs = mapper.reconcileData(jsonJobs, xmlJobs);

// Detect cycles
List<List<Integer>> cycles = mapper.detectCircularDependencies(reconciledJobs);

// Map to RMJ format
List<RmjJob> rmjJobs = mapper.mapToRmj(reconciledJobs);
```

## Running Tests

```bash
mvn test
```

All 10 tests pass successfully.

