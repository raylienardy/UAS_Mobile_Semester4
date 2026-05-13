package com.example.foodcourtgo.model;

public class AkunModel {
    private String userId;
    private String username;
    private String name;
    private String email;
    private Object pass;          // Bisa String atau Long
    private String role;
    private String tenantId;
    private boolean isActive;
    private String createdAt;

    public AkunModel() {}

    // Getter mengembalikan String
    public String getPass() {
        return pass == null ? "" : pass.toString();
    }

    // Hanya SATU setter dengan tipe Object
    public void setPass(Object pass) {
        this.pass = pass;
    }

    // Getter & Setter lainnya (tanpa perubahan)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}