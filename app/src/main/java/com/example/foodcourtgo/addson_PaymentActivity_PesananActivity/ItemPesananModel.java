package com.example.foodcourtgo.addson_PaymentActivity_PesananActivity;

public class ItemPesananModel {
    private String menuId;
    private String nama;
    private int qty;
    private long harga;
    private String opsi;
    private long hargaTambahan;

    public ItemPesananModel() {}

    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public long getHarga() { return harga; }
    public void setHarga(long harga) { this.harga = harga; }

    public String getOpsi() { return opsi; }
    public void setOpsi(String opsi) { this.opsi = opsi; }

    public long getHargaTambahan() { return hargaTambahan; }
    public void setHargaTambahan(long hargaTambahan) { this.hargaTambahan = hargaTambahan; }
}