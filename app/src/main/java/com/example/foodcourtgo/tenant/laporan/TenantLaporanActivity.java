package com.example.foodcourtgo.tenant.laporan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.LaporanPesananAdapter;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TenantLaporanActivity extends AppCompatActivity {

    Spinner spinnerBulan, spinnerTahun;
    TextView tvTotalPendapatan, tvJumlahPesanan;
    RecyclerView rvPesanan;
    LaporanPesananAdapter adapter;
    List<PesananAdminModel> pesananList = new ArrayList<>();

    String tenantId;
    DatabaseReference pesananRef;
    ValueEventListener pesananListener;
    List<PesananAdminModel> semuaPesanan = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_laporan);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        spinnerBulan = findViewById(R.id.spinner_bulan);
        spinnerTahun = findViewById(R.id.spinner_tahun);
        tvTotalPendapatan = findViewById(R.id.tv_total_pendapatan);
        tvJumlahPesanan = findViewById(R.id.tv_jumlah_pesanan);
        rvPesanan = findViewById(R.id.rv_laporan_pesanan);
        rvPesanan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LaporanPesananAdapter(pesananList);
        rvPesanan.setAdapter(adapter);

        // Spinner bulan
        ArrayAdapter<String> bulanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"});
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBulan.setAdapter(bulanAdapter);

        // Spinner tahun (5 tahun terakhir)
        int tahunSekarang = Calendar.getInstance().get(Calendar.YEAR);
        List<String> tahunList = new ArrayList<>();
        for (int i = tahunSekarang; i >= tahunSekarang - 4; i--) {
            tahunList.add(String.valueOf(i));
        }
        ArrayAdapter<String> tahunAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tahunList);
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTahun.setAdapter(tahunAdapter);
        spinnerTahun.setSelection(0);

        // Load data pesanan dari Firebase (hanya yang status done)
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        pesananListener = pesananRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        semuaPesanan.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PesananAdminModel p = ds.getValue(PesananAdminModel.class);
                            if (p != null && "done".equals(p.getStatus())) {
                                p.setId(ds.getKey());
                                semuaPesanan.add(p);
                            }
                        }
                        applyFilter();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TenantLaporanActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Listeners untuk filter
        spinnerBulan.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { applyFilter(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        spinnerTahun.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { applyFilter(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        findViewById(R.id.btn_back_laporan).setOnClickListener(v -> finish());
    }

    private void applyFilter() {
        int bulan = spinnerBulan.getSelectedItemPosition() + 1; // Jan = 1
        int tahun = Integer.parseInt(spinnerTahun.getSelectedItem().toString());

        String[] bulanNames = {"Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"};
        List<PesananAdminModel> filtered = new ArrayList<>();
        long totalPendapatan = 0;
        for (PesananAdminModel p : semuaPesanan) {
            String waktu = p.getWaktu();
            if (waktu != null && waktu.contains(String.valueOf(tahun))) {
                for (int i = 0; i < bulanNames.length; i++) {
                    if (waktu.contains(bulanNames[i]) && (i+1) == bulan) {
                        filtered.add(p);
                        totalPendapatan += p.getTotalHarga();
                        break;
                    }
                }
            }
        }
        tvTotalPendapatan.setText("Rp " + String.format("%,d", totalPendapatan));
        tvJumlahPesanan.setText(String.valueOf(filtered.size()));
        adapter.updateList(filtered);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pesananListener != null) pesananRef.removeEventListener(pesananListener);
    }
}