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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    List<PesananAdminModel> semuaPesanan = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_laporan);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        if (tenantId.isEmpty()) {
            Toast.makeText(this, "Tenant ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        spinnerBulan = findViewById(R.id.spinner_bulan);
        spinnerTahun = findViewById(R.id.spinner_tahun);
        tvTotalPendapatan = findViewById(R.id.tv_total_pendapatan);
        tvJumlahPesanan = findViewById(R.id.tv_jumlah_pesanan);
        rvPesanan = findViewById(R.id.rv_laporan_pesanan);
        rvPesanan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LaporanPesananAdapter(pesananList);
        rvPesanan.setAdapter(adapter);

        String[] bulanArray = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        ArrayAdapter<String> bulanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bulanArray);
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBulan.setAdapter(bulanAdapter);

        int tahunSekarang = Calendar.getInstance().get(Calendar.YEAR);
        List<String> tahunList = new ArrayList<>();
        for (int i = tahunSekarang; i >= tahunSekarang - 4; i--) {
            tahunList.add(String.valueOf(i));
        }
        ArrayAdapter<String> tahunAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tahunList);
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTahun.setAdapter(tahunAdapter);
        spinnerTahun.setSelection(0);

        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        pesananRef.orderByChild("tenantId").equalTo(tenantId).addValueEventListener(new ValueEventListener() {
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
                filterByMonthYear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TenantLaporanActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });

        spinnerBulan.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { filterByMonthYear(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        spinnerTahun.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { filterByMonthYear(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        findViewById(R.id.btn_back_laporan).setOnClickListener(v -> finish());
    }

    private void filterByMonthYear() {
        if (semuaPesanan.isEmpty()) {
            tvTotalPendapatan.setText("Rp 0");
            tvJumlahPesanan.setText("0");
            adapter.updateList(new ArrayList<>());
            return;
        }

        int bulan = spinnerBulan.getSelectedItemPosition() + 1;
        int tahun = Integer.parseInt(spinnerTahun.getSelectedItem().toString());

        // Format tanggal yang disimpan di Firebase: "dd MMM yyyy, HH:mm" (contoh: "14 May 2026, 21:54")
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US);

        List<PesananAdminModel> filtered = new ArrayList<>();
        long total = 0;

        for (PesananAdminModel p : semuaPesanan) {
            String waktuStr = p.getWaktu();
            if (waktuStr == null || waktuStr.isEmpty()) continue;

            try {
                Date date = sdf.parse(waktuStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int tahunPesanan = cal.get(Calendar.YEAR);
                int bulanPesanan = cal.get(Calendar.MONTH) + 1;

                if (tahunPesanan == tahun && bulanPesanan == bulan) {
                    filtered.add(p);
                    total += p.getTotalHarga();
                }
            } catch (ParseException e) {
                // Format tidak sesuai (misal hanya "14.05" dari pesanan lama), abaikan
            }
        }

        tvTotalPendapatan.setText("Rp " + String.format("%,d", total));
        tvJumlahPesanan.setText(String.valueOf(filtered.size()));
        adapter.updateList(filtered);
    }
}