package com.example.foodcourtgo.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String menuId;
    private String nama;
    private long harga;
    private String opsi;        // misal "pedas level 2"
    private long hargaTambahan;
    private int qty;
    private String catatan;     // catatan tambahan dari pembeli

    public CartItem() {}

    public CartItem(String menuId, String nama, long harga, String opsi, long hargaTambahan, int qty, String catatan) {
        this.menuId = menuId;
        this.nama = nama;
        this.harga = harga;
        this.opsi = opsi;
        this.hargaTambahan = hargaTambahan;
        this.qty = qty;
        this.catatan = catatan;
    }

    // Getter dan Setter
    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public long getHarga() { return harga; }
    public void setHarga(long harga) { this.harga = harga; }

    public String getOpsi() { return opsi; }
    public void setOpsi(String opsi) { this.opsi = opsi; }

    public long getHargaTambahan() { return hargaTambahan; }
    public void setHargaTambahan(long hargaTambahan) { this.hargaTambahan = hargaTambahan; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public long getTotalHarga() {
        return (harga + hargaTambahan) * qty;
    }
}