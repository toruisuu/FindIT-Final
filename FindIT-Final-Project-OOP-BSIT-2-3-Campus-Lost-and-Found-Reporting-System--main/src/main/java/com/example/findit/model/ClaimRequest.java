package com.example.findit.model;

public class ClaimRequest {
    private final int id;
    private final ItemReport item;
    private final String claimantName;
    private final String studentNumber;
    private final String contactInfo;
    private final String proofDescription;
    private String status;
    private String trackingId;


    public ClaimRequest(int id, ItemReport item, String claimantName, String studentNumber,
                        String contactInfo, String proofDescription, String status, String trackingId) {
        this.id = id;
        this.item = item;
        this.claimantName = claimantName;
        this.studentNumber = studentNumber;
        this.contactInfo = contactInfo;
        this.proofDescription = proofDescription;
        this.status = status;
        this.trackingId = trackingId;
    }

    public int getId() {
        return id;
    }

    public ItemReport getItem() {
        return item;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getProofDescription() {
        return proofDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrackingId() {
        return trackingId;
    }
}
