package com.example.foodcourtgo.model;

public class AkunModel {
    private String userId;      // key di Firebase (A0001, TENANT001, ADMIN001)
    private String username;
    private String name;
    private String email;
    private String pass;        // bisa disimpan plain untuk demo, di produksi hash
    private String role;        // "customer", "tenant", "super_admin"
    private String tenantId;    // hanya untuk role tenant, referensi ke node tenant
    private boolean isActive;
    private String createdAt;

    public AkunModel() {}

    // Constructor, getter, setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}