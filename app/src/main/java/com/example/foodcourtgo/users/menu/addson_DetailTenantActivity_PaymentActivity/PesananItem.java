package com.example.foodcourtgo.users.menu.addson_DetailTenantActivity_PaymentActivity;

public class PesananItem {
    private String menuId;
    private String nama;
    private long harga;
    private String opsi;          // contoh: "Pedas, Porsi Besar"
    private long hargaTambahan;   // akumulasi tambahan

    public PesananItem(String menuId, String nama, long harga, String opsi, long hargaTambahan) {
        this.menuId = menuId;
        this.nama = nama;
        this.harga = harga;
        this.opsi = opsi;
        this.hargaTambahan = hargaTambahan;
    }

    public String getMenuId() { return menuId; }
    public String getNama() { return nama; }
    public long getHarga() { return harga; }
    public String getOpsi() { return opsi; }
    public long getHargaTambahan() { return hargaTambahan; }

    public long getTotalHarga() { return harga + hargaTambahan; }
}