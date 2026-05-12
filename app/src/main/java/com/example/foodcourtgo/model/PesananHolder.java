package com.example.foodcourtgo.model;

import java.util.ArrayList;
import java.util.List;

public class PesananHolder {
    private static List<PesananItem> pesananList = new ArrayList<>();

    public static List<PesananItem> getPesananList() {
        return pesananList;
    }

    public static void addItem(PesananItem item) {
        pesananList.add(item);
    }

    public static void clear() {
        pesananList.clear();
    }

    public static class TambahanModel {
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
}