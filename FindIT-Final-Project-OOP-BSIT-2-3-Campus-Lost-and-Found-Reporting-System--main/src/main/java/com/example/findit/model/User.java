package com.example.findit.model;

public class User {

    private int userId;
    private String idNumber;
    private String fullName;
    private String password;
    private String role; // "Student" or "Admin"
    private String contactNumber;

    public User() {}

    public User(String idNumber, String fullName, String password, String role, String contactNumber) {
        this.idNumber = idNumber;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.contactNumber = contactNumber;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getIdNumber() { return idNumber; }
    public String getFullName() { return fullName; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getContactNumber() { return contactNumber; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}