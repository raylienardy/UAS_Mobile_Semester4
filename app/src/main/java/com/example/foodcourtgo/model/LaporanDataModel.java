package com.example.foodcourtgo.model;

public class LaporanDataModel {
    private long totalPendapatan;
    private int totalPesanan;
    private int[] dataPeriode; // array untuk grafik (misal 7 hari atau 12 bulan)

    public LaporanDataModel() {}

    public long getTotalPendapatan() { return totalPendapatan; }
    public void setTotalPendapatan(long totalPendapatan) { this.totalPendapatan = totalPendapatan; }

    public int getTotalPesanan() { return totalPesanan; }
    public void setTotalPesanan(int totalPesanan) { this.totalPesanan = totalPesanan; }

    public int[] getDataPeriode() { return dataPeriode; }
    public void setDataPeriode(int[] dataPeriode) { this.dataPeriode = dataPeriode; }
}