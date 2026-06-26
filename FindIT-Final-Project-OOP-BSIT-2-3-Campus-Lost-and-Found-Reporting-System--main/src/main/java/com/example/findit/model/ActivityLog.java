package com.example.findit.model;

import java.time.LocalDateTime;

public class ActivityLog {

    private int logId;
    private int userId;
    private String action;
    private String description;
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public int           getLogId()       { return logId; }
    public int           getUserId()      { return userId; }
    public String        getAction()      { return action; }
    public String        getDescription() { return description; }
    public LocalDateTime getCreatedAt()   { return createdAt; }

    public void setLogId(int logId)                   { this.logId       = logId; }
    public void setUserId(int userId)                 { this.userId      = userId; }
    public void setAction(String action)              { this.action      = action; }
    public void setDescription(String description)    { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt   = createdAt; }
}