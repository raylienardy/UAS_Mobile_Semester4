package com.example.foodcourtgo.users.menu.addson_DetailTenantActivity;

import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity_PaymentActivity.PesananHolder;

import java.util.List;

public class MenuModel {
    private String menuId;
    private String nama;
    private String deskripsi;
    private long harga;
    private String gambar;
    private String tenantId;
    private String kategori;           // baru
    private List<PesananHolder.TambahanModel> tambahan;

    public MenuModel() {}

    // Getter & Setter
    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public long getHarga() { return harga; }
    public void setHarga(long harga) { this.harga = harga; }

    public String getGambar() { return gambar; }
    public void setGambar(String gambar) { this.gambar = gambar; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public List<PesananHolder.TambahanModel> getTambahan() { return tambahan; }
    public void setTambahan(List<PesananHolder.TambahanModel> tambahan) { this.tambahan = tambahan; }

    public String getHargaFormatted() {
        return "Rp" + String.format("%,d", harga).replace(',', '.');
    }
}