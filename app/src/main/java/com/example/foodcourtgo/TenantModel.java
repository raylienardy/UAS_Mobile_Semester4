package com.example.foodcourtgo;

public class TenantModel {
    private String id;
    private String nama;
    private String deskripsi;
    private String kategori;
    private String gambar;
    private String status;
    private String email;
    private String telepon;
    private String lokasi;
    private String namaPemilik;  // opsional

    // Constructor kosong wajib ada untuk Firebase
    public TenantModel() {}

    public TenantModel(String id, String nama, String deskripsi, String kategori, String gambar, String status, String email, String telepon, String lokasi, String namaPemilik) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.kategori = kategori;
        this.status = status;
        this.email = email;
        this.telepon = telepon;
        this.lokasi = lokasi;
        this.namaPemilik = namaPemilik;
        this.gambar = gambar;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getGambar() { return gambar; }
    public void setGambar(String gambar) { this.gambar = gambar; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getNamaPemilik() { return namaPemilik; }
    public void setNamaPemilik(String namaPemilik) { this.namaPemilik = namaPemilik; }
}