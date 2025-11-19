package com.tana.migration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tana.migration.model.PartInfo;
import com.tana.migration.model.PartTransaction;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Optimized processor for EOD parts consumption data.
 * 
 * PERFORMANCE FIXES:
 * 1. Uses HashMap for O(1) part lookup instead of ArrayList.contains() - O(n)
 * 2. Uses HashSet for tracking unique values instead of ArrayList.contains()
 * 3. Single pass through data where possible
 * 
 * This is the refactored version that fixes the performance bottlenecks.
 */
public class PartsConsumptionProcessorOptimized {
    
    private final ObjectMapper objectMapper;
    
    public PartsConsumptionProcessorOptimized() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Processes the parts consumption file and enriches transactions with part information.
     * 
     * PERFORMANCE FIX: Uses HashMap for O(1) lookup instead of nested loop O(n*m)
     * 
     * @param filePath Path to the EOD parts consumption JSON file
     * @return List of enriched transactions
     */
    public List<PartsConsumptionProcessor.EnrichedTransaction> processPartsConsumption(String filePath) throws IOException {
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
        
        // Parse parts catalog into HashMap for O(1) lookup
        JsonNode catalogNode = root.get("part_master_catalog");
        // PERFORMANCE FIX: HashMap instead of ArrayList - O(1) lookup vs O(n)
        Map<String, PartInfo> partsCatalogMap = new HashMap<>();
        if (catalogNode != null && catalogNode.isArray()) {
            for (JsonNode partNode : catalogNode) {
                PartInfo part = objectMapper.treeToValue(partNode, PartInfo.class);
                partsCatalogMap.put(part.getPartSku(), part); // Key: part_sku, Value: PartInfo
            }
        }
        
        // Enrich transactions with part information
        List<PartsConsumptionProcessor.EnrichedTransaction> enrichedTransactions = new ArrayList<>();
        
        // PERFORMANCE FIX: Single loop with HashMap lookup - O(n) instead of O(n*m)
        for (PartTransaction transaction : transactions) {
            // PERFORMANCE FIX: HashMap.get() is O(1) instead of linear search O(n)
            PartInfo matchingPart = partsCatalogMap.get(transaction.getPartSkuRef());
            
            PartsConsumptionProcessor.EnrichedTransaction enriched = new PartsConsumptionProcessor.EnrichedTransaction();
            enriched.setTransaction(transaction);
            enriched.setPartInfo(matchingPart);
            enrichedTransactions.add(enriched);
        }
        
        return enrichedTransactions;
    }
    
    /**
     * Gets summary statistics for parts consumption.
     * 
     * PERFORMANCE FIX: Uses HashSet for O(1) add/contains operations
     * 
     * @param filePath Path to the EOD parts consumption JSON file
     * @return Summary statistics
     */
    public PartsConsumptionProcessor.ConsumptionSummary getSummary(String filePath) throws IOException {
        List<PartsConsumptionProcessor.EnrichedTransaction> transactions = processPartsConsumption(filePath);
        
        PartsConsumptionProcessor.ConsumptionSummary summary = new PartsConsumptionProcessor.ConsumptionSummary();
        summary.setTotalTransactions(transactions.size());
        
        // PERFORMANCE FIX: HashSet instead of ArrayList
        // HashSet.add() and contains() are O(1) vs ArrayList.contains() which is O(n)
        Set<String> uniqueAssemblyLines = new HashSet<>();
        Set<String> uniquePartSkus = new HashSet<>();
        
        int totalQuantity = 0;
        
        // Single pass through transactions
        for (PartsConsumptionProcessor.EnrichedTransaction enriched : transactions) {
            PartTransaction transaction = enriched.getTransaction();
            
            // PERFORMANCE FIX: HashSet.add() is O(1), no need to check contains() first
            if (transaction.getAssemblyLine() != null) {
                uniqueAssemblyLines.add(transaction.getAssemblyLine());
            }
            
            // PERFORMANCE FIX: HashSet.add() is O(1)
            if (transaction.getPartSkuRef() != null) {
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
}

