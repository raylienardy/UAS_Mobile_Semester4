package com.example.foodcourtgo.users.menu.addson_MenuOptionActivity;

public class TambahanModel {
    private String nama;
    private long harga;

    public TambahanModel() {}

    public TambahanModel(String nama, long harga) {
        this.nama = nama;
        this.harga = harga;
    }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public long getHarga() { return harga; }
    public void setHarga(long harga) { this.harga = harga; }
}