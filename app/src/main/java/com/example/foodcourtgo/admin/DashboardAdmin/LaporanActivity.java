package com.example.foodcourtgo.admin.DashboardAdmin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LaporanActivity extends AppCompatActivity {

    // ── TextView untuk menampilkan ringkasan laporan ──
    private TextView tvReportIncome;   // Total pendapatan
    private TextView tvReportOrder;    // Total jumlah pesanan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan activity dengan layout laporan
        setContentView(R.layout.admin_activity_laporan);

        // ── Inisialisasi TextView ──────────────────────
        tvReportIncome = findViewById(R.id.tv_report_income);
        tvReportOrder  = findViewById(R.id.tv_report_order);

        // ── Ambil data dari Firebase node "pesanan" ────
        DatabaseReference pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Hitung jumlah pesanan = jumlah anak di node "pesanan"
                int totalOrders = (int) snapshot.getChildrenCount();
                long totalIncome = 0;

                // Loop setiap pesanan untuk menjumlahkan totalHarga
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Object totalObj = snap.child("totalHarga").getValue();
                    if (totalObj != null) {
                        totalIncome += (long) totalObj;  // Akumulasi pendapatan
                    }
                }

                // Tampilkan hasil ke layar
                tvReportOrder.setText(String.valueOf(totalOrders));
                // Format pendapatan dengan pemisah ribuan (contoh: Rp 3.200.000)
                tvReportIncome.setText("Rp " + String.format("%,d", totalIncome).replace(',', '.'));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Jika gagal mengambil data, bisa ditambahkan Toast
            }
        });

        // ── Tombol "Export Data" ──────────────────────
        // Saat ini masih simulasi, menampilkan Toast saja
        findViewById(R.id.btn_export_report).setOnClickListener(v ->
                Toast.makeText(this, "Laporan diekspor", Toast.LENGTH_SHORT).show());

        // ── Filter laporan ────────────────────────────
        // Ketiga tombol ini masih dalam tahap "segera hadir"
        findViewById(R.id.filter_daily).setOnClickListener(v ->
                Toast.makeText(this, "Filter harian", Toast.LENGTH_SHORT).show());

        findViewById(R.id.filter_monthly).setOnClickListener(v ->
                Toast.makeText(this, "Filter bulanan", Toast.LENGTH_SHORT).show());

        findViewById(R.id.filter_yearly).setOnClickListener(v ->
                Toast.makeText(this, "Filter tahunan", Toast.LENGTH_SHORT).show());
    }
}