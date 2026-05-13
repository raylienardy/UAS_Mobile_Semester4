package com.example.foodcourtgo.model;

public class MejaModel {
    private String id;          // key di Firebase, misal "M001"
    private int nomor;          // nomor meja (misal 1,2,3...)
    private String lokasi;      // "Area Barat", "Area Timur", dll.
    private String qrCode;      // string yang akan diencode jadi QR
    private String status;      // "available" atau "occupied"

    // Constructor kosong wajib untuk Firebase
    public MejaModel() {}

    public MejaModel(String id, int nomor, String lokasi, String qrCode, String status) {
        this.id = id;
        this.nomor = nomor;
        this.lokasi = lokasi;
        this.qrCode = qrCode;
        this.status = status;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNomor() { return nomor; }
    public void setNomor(int nomor) { this.nomor = nomor; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}