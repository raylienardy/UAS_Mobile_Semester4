package com.example.foodcourtgo.admin.TenantManagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
// Mengimpor model TenantModel dari package addson yang digunakan bersama
import com.example.foodcourtgo.addson.TenantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminAddTenantActivity extends AppCompatActivity {

    // ── Form input ───────────────────────────────
    private EditText etNama;          // Input nama tenant
    private EditText etDeskripsi;     // Input deskripsi
    private EditText etKategori;      // Input kategori
    private EditText etGambar;        // Input URL gambar (opsional)
    private Button btnSimpan;         // Tombol simpan

    // ── Referensi Firebase ──────────────────────
    private DatabaseReference tenantRef;  // Untuk mengakses node "tenant"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_admin_add_tenant); // Layout form tambah tenant

        // ── Inisialisasi view dari layout ────────
        ImageView ivBack = findViewById(R.id.ivBack);      // Tombol kembali di header
        etNama = findViewById(R.id.etNama);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        etKategori = findViewById(R.id.etKategori);
        etGambar = findViewById(R.id.etGambar);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Referensi ke node "tenant" Firebase
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        // ── Tombol kembali (panah kiri) ──────────
        // Menutup activity ini dan kembali ke TenantManagementActivity
        ivBack.setOnClickListener(v -> finish());

        // ── Tombol simpan tenant ──────────────────
        btnSimpan.setOnClickListener(v -> {
            // Ambil teks dari form dan hapus spasi di ujung
            String nama = etNama.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String kategori = etKategori.getText().toString().trim();
            String gambar = etGambar.getText().toString().trim();

            // Validasi: nama tenant wajib diisi
            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tenant harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // ── Membuat ID tenant otomatis ────────────
            // Cari ID terakhir di Firebase untuk generate ID baru (T0001, T0002, ...)
            tenantRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int nextNumber = 1; // Default jika belum ada tenant
                    // Loop untuk mengambil key terakhir
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String key = data.getKey();
                        // Key tenant diawali 'T', ambil angkanya
                        if (key != null && key.startsWith("T")) {
                            try {
                                int num = Integer.parseInt(key.substring(1));
                                nextNumber = num + 1; // Tambah 1 dari nomor terakhir
                            } catch (NumberFormatException e) {
                                // Abaikan jika format salah
                            }
                        }
                    }
                    // Format ID menjadi T0001, T0002, …
                    String newId = "T" + String.format("%04d", nextNumber);

                    // Buat objek TenantModel dengan data dari form
                    TenantModel tenant = new TenantModel();
                    tenant.setId(newId);
                    tenant.setNama(nama);
                    tenant.setDeskripsi(deskripsi);
                    tenant.setKategori(kategori);
                    tenant.setGambar(gambar.isEmpty() ? "" : gambar); // Kosongkan bila tidak diisi
                    tenant.setStatus("active"); // Tenant baru langsung aktif

                    // Simpan ke Firebase
                    tenantRef.child(newId).setValue(tenant)
                            .addOnSuccessListener(unused -> {
                                // Berhasil simpan, tampilkan pesan & tutup activity
                                Toast.makeText(AdminAddTenantActivity.this, "Tenant berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Gagal menyimpan
                                Toast.makeText(AdminAddTenantActivity.this, "Gagal menambahkan tenant", Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Error saat mengambil ID terakhir
                    Toast.makeText(AdminAddTenantActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}