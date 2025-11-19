package com.tana.migration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents part information from the parts master catalog.
 */
public class PartInfo {
    @JsonProperty("part_sku")
    private String partSku;
    
    private String supplier;
    
    @JsonProperty("cost_center")
    private String costCenter;
    
    public PartInfo() {
    }
    
    public PartInfo(String partSku, String supplier, String costCenter) {
        this.partSku = partSku;
        this.supplier = supplier;
        this.costCenter = costCenter;
    }
    
    public String getPartSku() {
        return partSku;
    }
    
    public void setPartSku(String partSku) {
        this.partSku = partSku;
    }
    
    public String getSupplier() {
        return supplier;
    }
    
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    public String getCostCenter() {
        return costCenter;
    }
    
    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartInfo partInfo = (PartInfo) o;
        return Objects.equals(partSku, partInfo.partSku);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(partSku);
    }
}

