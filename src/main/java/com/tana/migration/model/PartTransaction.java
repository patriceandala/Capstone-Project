package com.tana.migration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a parts consumption transaction from the EOD parts consumption file.
 */
public class PartTransaction {
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("assembly_line")
    private String assemblyLine;
    
    @JsonProperty("part_sku_ref")
    private String partSkuRef;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    public PartTransaction() {
    }
    
    public PartTransaction(String transactionId, String assemblyLine, String partSkuRef, Integer quantity) {
        this.transactionId = transactionId;
        this.assemblyLine = assemblyLine;
        this.partSkuRef = partSkuRef;
        this.quantity = quantity;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getAssemblyLine() {
        return assemblyLine;
    }
    
    public void setAssemblyLine(String assemblyLine) {
        this.assemblyLine = assemblyLine;
    }
    
    public String getPartSkuRef() {
        return partSkuRef;
    }
    
    public void setPartSkuRef(String partSkuRef) {
        this.partSkuRef = partSkuRef;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartTransaction that = (PartTransaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}

