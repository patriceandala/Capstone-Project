package com.tana.migration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tana.migration.model.PartInfo;
import com.tana.migration.model.PartTransaction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Processor for EOD parts consumption data.
 * 
 * PERFORMANCE BUGS INTENTIONALLY INTRODUCED:
 * 1. Using ArrayList.contains() in nested loops - O(n*m) complexity
 * 2. Linear search through ArrayList instead of HashMap lookup
 * 3. Inefficient data structure choices
 * 
 * This class demonstrates performance issues that will be identified and fixed.
 */
public class PartsConsumptionProcessor {
    
    private final ObjectMapper objectMapper;
    
    public PartsConsumptionProcessor() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Processes the parts consumption file and enriches transactions with part information.
     * 
     * PERFORMANCE BUG: Uses ArrayList.contains() in nested loop - O(n*m) complexity
     * For large datasets, this becomes extremely slow.
     * 
     * @param filePath Path to the EOD parts consumption JSON file
     * @return List of enriched transactions
     */
    public List<EnrichedTransaction> processPartsConsumption(String filePath) throws IOException {
        File file = new File(filePath);
        JsonNode root = objectMapper.readTree(file);
        
        // Parse transactions
        JsonNode transactionsNode = root.get("daily_consumption_log");
        List<PartTransaction> transactions = new ArrayList<>();
        if (transactionsNode != null && transactionsNode.isArray()) {
            for (JsonNode transactionNode : transactionsNode) {
                PartTransaction transaction = objectMapper.treeToValue(transactionNode, PartTransaction.class);
                transactions.add(transaction);
            }
        }
        
        // Parse parts catalog
        JsonNode catalogNode = root.get("part_master_catalog");
        List<PartInfo> partsCatalog = new ArrayList<>(); // PERFORMANCE BUG: Should use HashMap
        if (catalogNode != null && catalogNode.isArray()) {
            for (JsonNode partNode : catalogNode) {
                PartInfo part = objectMapper.treeToValue(partNode, PartInfo.class);
                partsCatalog.add(part);
            }
        }
        
        // Enrich transactions with part information
        List<EnrichedTransaction> enrichedTransactions = new ArrayList<>();
        
        // PERFORMANCE BUG: Nested loop with ArrayList.contains() - O(n*m) complexity
        // For each transaction, search through entire parts catalog
        for (PartTransaction transaction : transactions) {
            PartInfo matchingPart = null;
            
            // PERFORMANCE BUG: Linear search - O(n) for each transaction
            // Should use HashMap lookup - O(1)
            for (PartInfo part : partsCatalog) {
                if (part.getPartSku().equals(transaction.getPartSkuRef())) {
                    matchingPart = part;
                    break; // Found, but still inefficient
                }
            }
            
            EnrichedTransaction enriched = new EnrichedTransaction();
            enriched.setTransaction(transaction);
            enriched.setPartInfo(matchingPart);
            enrichedTransactions.add(enriched);
        }
        
        return enrichedTransactions;
    }
    
    /**
     * Gets summary statistics for parts consumption.
     * 
     * PERFORMANCE BUG: Multiple passes through data, using ArrayList.contains()
     * 
     * @param filePath Path to the EOD parts consumption JSON file
     * @return Summary statistics
     */
    public ConsumptionSummary getSummary(String filePath) throws IOException {
        List<EnrichedTransaction> transactions = processPartsConsumption(filePath);
        
        ConsumptionSummary summary = new ConsumptionSummary();
        summary.setTotalTransactions(transactions.size());
        
        // PERFORMANCE BUG: Using ArrayList to track unique values
        // ArrayList.contains() is O(n) - should use HashSet
        List<String> uniqueAssemblyLines = new ArrayList<>();
        List<String> uniquePartSkus = new ArrayList<>();
        
        int totalQuantity = 0;
        
        for (EnrichedTransaction enriched : transactions) {
            PartTransaction transaction = enriched.getTransaction();
            
            // PERFORMANCE BUG: ArrayList.contains() is O(n) for each check
            if (!uniqueAssemblyLines.contains(transaction.getAssemblyLine())) {
                uniqueAssemblyLines.add(transaction.getAssemblyLine());
            }
            
            // PERFORMANCE BUG: Another ArrayList.contains() check
            if (transaction.getPartSkuRef() != null && 
                !uniquePartSkus.contains(transaction.getPartSkuRef())) {
                uniquePartSkus.add(transaction.getPartSkuRef());
            }
            
            if (transaction.getQuantity() != null) {
                totalQuantity += transaction.getQuantity();
            }
        }
        
        summary.setUniqueAssemblyLines(uniqueAssemblyLines.size());
        summary.setUniquePartSkus(uniquePartSkus.size());
        summary.setTotalQuantity(totalQuantity);
        
        return summary;
    }
    
    /**
     * Represents an enriched transaction with part information.
     * Made public static for use by optimized processor.
     */
    public static class EnrichedTransaction {
        private PartTransaction transaction;
        private PartInfo partInfo;
        
        public PartTransaction getTransaction() {
            return transaction;
        }
        
        public void setTransaction(PartTransaction transaction) {
            this.transaction = transaction;
        }
        
        public PartInfo getPartInfo() {
            return partInfo;
        }
        
        public void setPartInfo(PartInfo partInfo) {
            this.partInfo = partInfo;
        }
    }
    
    /**
     * Summary statistics for parts consumption.
     * Made public static for use by optimized processor.
     */
    public static class ConsumptionSummary {
        private int totalTransactions;
        private int uniqueAssemblyLines;
        private int uniquePartSkus;
        private int totalQuantity;
        
        public int getTotalTransactions() {
            return totalTransactions;
        }
        
        public void setTotalTransactions(int totalTransactions) {
            this.totalTransactions = totalTransactions;
        }
        
        public int getUniqueAssemblyLines() {
            return uniqueAssemblyLines;
        }
        
        public void setUniqueAssemblyLines(int uniqueAssemblyLines) {
            this.uniqueAssemblyLines = uniqueAssemblyLines;
        }
        
        public int getUniquePartSkus() {
            return uniquePartSkus;
        }
        
        public void setUniquePartSkus(int uniquePartSkus) {
            this.uniquePartSkus = uniquePartSkus;
        }
        
        public int getTotalQuantity() {
            return totalQuantity;
        }
        
        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
        
        @Override
        public String toString() {
            return String.format("ConsumptionSummary{totalTransactions=%d, uniqueAssemblyLines=%d, uniquePartSkus=%d, totalQuantity=%d}",
                    totalTransactions, uniqueAssemblyLines, uniquePartSkus, totalQuantity);
        }
    }
}

