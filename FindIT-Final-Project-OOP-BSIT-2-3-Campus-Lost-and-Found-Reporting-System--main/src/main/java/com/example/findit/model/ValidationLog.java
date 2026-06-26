package com.example.findit.model;

import java.time.LocalDateTime;

public class ValidationLog {

    private int validationId;
    private int itemId;
    private int validatedBy;
    private String validationType;
    private String remarks;
    private LocalDateTime validatedAt;

    public ValidationLog() {}

    public ValidationLog(int validationId, int itemId, int validatedBy,
                         String validationType, String remarks,
                         LocalDateTime validatedAt) {
        this.validationId   = validationId;
        this.itemId         = itemId;
        this.validatedBy    = validatedBy;
        this.validationType = validationType;
        this.remarks        = remarks;
        this.validatedAt    = validatedAt;
    }

    public int           getValidationId()   { return validationId; }
    public int           getItemId()         { return itemId; }
    public int           getValidatedBy()    { return validatedBy; }
    public String        getValidationType() { return validationType; }
    public String        getRemarks()        { return remarks; }
    public LocalDateTime getValidatedAt()    { return validatedAt; }

    public void setValidationId(int validationId)        { this.validationId   = validationId; }
    public void setItemId(int itemId)                    { this.itemId         = itemId; }
    public void setValidatedBy(int validatedBy)          { this.validatedBy    = validatedBy; }
    public void setValidationType(String validationType) { this.validationType = validationType; }
    public void setRemarks(String remarks)               { this.remarks        = remarks; }
    public void setValidatedAt(LocalDateTime validatedAt){ this.validatedAt    = validatedAt; }
}