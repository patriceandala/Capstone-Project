# Concurrency Challenge Test Files

This directory contains 90 diagnostic report JSON files (`diag_report_001.json` through `diag_report_090.json`) from the Day 2 concurrency challenge.

## File Schema

These files have a different schema than our `CompetitorJob` model:

```json
{
  "vin": "WDDE03EAFF783D143",
  "sequence_id": 1,
  "report_type": "ECU_FIRMWARE_UPDATE",
  "timestamp_utc": 1690000241,
  "status_code": "PENDING_UPDATE"
}
```

vs. our `CompetitorJob` schema:

```json
{
  "job_id": 1001,
  "job_name": "Test_Job_1",
  "dependencies": [...],
  "notes": "..."
}
```

## Purpose

These files are used to test concurrent parsing performance and error handling:

1. **Volume Testing**: Tests parsing of many files (90 files) concurrently
2. **Error Handling**: Demonstrates graceful error handling when files have wrong schema
3. **Thread Safety**: Verifies that errors are collected correctly across multiple threads
4. **Performance**: Shows concurrent vs sequential parsing comparison

## Usage

The `ConcurrentParsingPerformanceTest` uses these files to demonstrate:
- Concurrent parsing can handle many files efficiently
- Errors are collected and reported properly
- All files are processed (either successfully or with errors)
- Thread safety is maintained even when all files fail

## Note

Since these files have a different schema, they will all fail to parse with our `CompetitorJob` parser. This is intentional - it demonstrates that the concurrent parsing system handles errors gracefully and continues processing all files even when some (or all) fail.

