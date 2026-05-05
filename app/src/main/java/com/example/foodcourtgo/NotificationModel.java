package com.example.foodcourtgo;

public class NotificationModel {
    private String text;
    private String waktu;
    private String status;  // "unread" / "read"
    private String tenantId;

    public NotificationModel() {}  // wajib untuk Firebase

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getWaktu() { return waktu; }
    public void setWaktu(String waktu) { this.waktu = waktu; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}