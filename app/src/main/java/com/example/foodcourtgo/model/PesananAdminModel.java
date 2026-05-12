package com.example.foodcourtgo.model;


// PaymentActivity_PesananActivity
// terhubung kemana?
// - admin/Pesanan/DetailPesananActivity.java
// - users/menu/PaymentActivity.java
// - tenant/dashboard/TenantDashboardActivity.java
// - tenant/pesanan/TenantOrdersActivity.java
// - tenant/pesanan/TenantOrderDetailActivity.java

import java.util.List;

public class PesananAdminModel {
    private String id;
    private String customerId;
    private String customerName;
    private String tenantId;
    private String tenantNama;
    private List<ItemPesananModel> items;
    private long totalHarga;
    private String status;
    private String meja;
    private String waktu;

    public PesananAdminModel() {}

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTenantNama() { return tenantNama; }
    public void setTenantNama(String tenantNama) { this.tenantNama = tenantNama; }

    public List<ItemPesananModel> getItems() { return items; }
    public void setItems(List<ItemPesananModel> items) { this.items = items; }

    public long getTotalHarga() { return totalHarga; }
    public void setTotalHarga(long totalHarga) { this.totalHarga = totalHarga; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMeja() { return meja; }
    public void setMeja(String meja) { this.meja = meja; }

    public String getWaktu() { return waktu; }
    public void setWaktu(String waktu) { this.waktu = waktu; }
}