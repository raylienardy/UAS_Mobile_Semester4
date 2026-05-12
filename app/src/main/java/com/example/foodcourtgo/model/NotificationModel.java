package com.example.foodcourtgo.model;

public class NotificationModel {
    private String id;
    private String tenantId;
    private String text;
    private String waktu;
    private String status;   // "unread" / "read"

    public NotificationModel() {}

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getWaktu() { return waktu; }
    public void setWaktu(String waktu) { this.waktu = waktu; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}