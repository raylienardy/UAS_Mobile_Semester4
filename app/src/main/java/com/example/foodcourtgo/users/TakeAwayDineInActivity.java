package com.example.foodcourtgo.users;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.users.menu.DetailTenantActivity;

public class TakeAwayDineInActivity extends AppCompatActivity {

    // Deklarasi view yang ada di layout
    private ImageView ivBack;        // Tombol kembali (ikon panah)
    private Button btnTakeAway;      // Tombol untuk memilih "Take Away"
    private Button btnDineIn;        // Tombol untuk memilih "Dine In"

    // Variabel untuk menyimpan data tenant yang dikirim dari halaman sebelumnya
    private String tenantId, tenantNama, tenantGambar, tenantKategori, tenantDeskripsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan activity ini dengan file layout XML-nya
        setContentView(R.layout.users_activity_takeaway_dinein);

        // ── Inisialisasi view dari layout ─────────────────
        ivBack      = findViewById(R.id.ivBack);
        btnTakeAway = findViewById(R.id.btnTakeAway);
        btnDineIn   = findViewById(R.id.btnDineIn);

        // ── Ambil data tenant yang dikirim melalui Intent ─
        Intent intent = getIntent();                         // Ambil Intent pembuka activity ini
        tenantId          = intent.getStringExtra("tenantId");          // ID tenant
        tenantNama        = intent.getStringExtra("tenantNama");        // Nama tenant
        tenantGambar      = intent.getStringExtra("tenantGambar");      // URL/resource gambar tenant
        tenantKategori    = intent.getStringExtra("tenantKategori");    // Kategori tenant
        tenantDeskripsi   = intent.getStringExtra("tenantDeskripsi");   // Deskripsi tenant

        // ── Tombol Kembali (panah kiri) ───────────────────
        // Saat diklik, activity ini akan ditutup dan kembali ke halaman sebelumnya (HomeActivity)
        ivBack.setOnClickListener(v -> finish());

        // ── Tombol "Take Away" ────────────────────────────
        // Akan membuka DetailTenantActivity sambil membawa data tenant yang sama
        btnTakeAway.setOnClickListener(v -> {
            Intent menuIntent = new Intent(this, DetailTenantActivity.class);
            // Kirim semua data tenant ke halaman detail
            menuIntent.putExtra("tenantId", tenantId);
            menuIntent.putExtra("tenantNama", tenantNama);
            menuIntent.putExtra("tenantGambar", tenantGambar);
            menuIntent.putExtra("tenantKategori", tenantKategori);
            menuIntent.putExtra("tenantDeskripsi", tenantDeskripsi);
            startActivity(menuIntent);
        });

        // ── Tombol "Dine In" ─────────────────────────────
        // Akan membuka QrScannerActivity untuk memindai QR meja, data tenant tetap dibawa
        btnDineIn.setOnClickListener(v -> {
            Intent qrIntent = new Intent(TakeAwayDineInActivity.this, QrScannerActivity.class);
            qrIntent.putExtra("tenantId", tenantId);
            qrIntent.putExtra("tenantNama", tenantNama);
            qrIntent.putExtra("tenantGambar", tenantGambar);
            qrIntent.putExtra("tenantKategori", tenantKategori);
            qrIntent.putExtra("tenantDeskripsi", tenantDeskripsi);
            startActivity(qrIntent);
        });
    }
}