package com.example.foodcourtgo.model;

public class EmergencyModel {
    private String id;
    private String userId;
    private String userName;
    private String userRole; // "customer" atau "tenant"
    private String mejaId;   // untuk customer (opsional)
    private String tenantId; // untuk tenant (opsional)
    private String pesan;    // pesan singkat
    private String timestamp;
    private String status;   // "pending" atau "resolved"

    public EmergencyModel() {}

    // Constructor, getter, setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getMejaId() { return mejaId; }
    public void setMejaId(String mejaId) { this.mejaId = mejaId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPesan() { return pesan; }
    public void setPesan(String pesan) { this.pesan = pesan; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}