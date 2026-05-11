package com.example.foodcourtgo.admin.MenuManagement;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.addson.TenantModel;
import com.example.foodcourtgo.addson.MenuModel;
import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity_PaymentActivity.PesananHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminAddMenuActivity extends AppCompatActivity {

    // ── Form input utama ───────────────────────────
    private EditText etNama, etDeskripsi, etHarga, etGambar;
    // ── Input untuk menambah opsi tambahan (bisa banyak) ──
    private EditText etTambahanNama, etTambahanHarga;
    // ── Spinner pemilihan tenant ───────────────────
    private Spinner spinnerTenant;
    // ── Tombol simpan & tambah tambahan ────────────
    private Button btnSimpan, btnTambahTambahan;
    // ── Teks yang menampilkan daftar tambahan yg sudah ditambahkan ──
    private TextView tvTambahanList;

    // ── Data tenant untuk spinner ──────────────────
    private List<TenantModel> tenantList = new ArrayList<>();
    // ── Buffer tambahan yang siap disimpan ──────────
    private List<PesananHolder.TambahanModel> tambahanBuffer = new ArrayList<>();
    // ── Adapter spinner (hanya nama tenant) ────────
    private ArrayAdapter<String> tenantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_admin_add_menu);

        // ── Inisialisasi view ──────────────────────
        ImageView ivBack = findViewById(R.id.ivBack);
        spinnerTenant = findViewById(R.id.spinnerTenant);
        etNama = findViewById(R.id.etMenuNama);
        etDeskripsi = findViewById(R.id.etMenuDeskripsi);
        etHarga = findViewById(R.id.etMenuHarga);
        etGambar = findViewById(R.id.etMenuGambar);
        etTambahanNama = findViewById(R.id.etTambahanNama);
        etTambahanHarga = findViewById(R.id.etTambahanHarga);
        btnTambahTambahan = findViewById(R.id.btnTambahTambahan);
        btnSimpan = findViewById(R.id.btnSimpan);
        tvTambahanList = findViewById(R.id.tvTambahanList);

        // ── Klik tombol kembali → tutup activity ini ──
        ivBack.setOnClickListener(v -> finish());

        // ═══════════════════════════════════════════════
        // Load spinner tenant dari Firebase
        // ═══════════════════════════════════════════════
        DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
        // Buat adapter sederhana untuk Spinner
        tenantAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTenant.setAdapter(tenantAdapter);

        // Ambil data tenant satu kali
        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();
                List<String> names = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    TenantModel t = snap.getValue(TenantModel.class);
                    if (t != null) {
                        t.setId(snap.getKey());       // Simpan key sebagai ID tenant
                        tenantList.add(t);            // Simpan objek tenant lengkap
                        names.add(t.getNama());       // Ambil nama untuk ditampilkan di spinner
                    }
                }
                // Masukkan nama-nama tenant ke adapter spinner
                tenantAdapter.clear();
                tenantAdapter.addAll(names);
                tenantAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // ═══════════════════════════════════════════════
        // Tombol "+ Tambahkan" untuk opsi tambahan
        // ═══════════════════════════════════════════════
        btnTambahTambahan.setOnClickListener(v -> {
            String nama = etTambahanNama.getText().toString().trim();
            String hargaStr = etTambahanHarga.getText().toString().trim();
            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tambahan harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            long harga = 0;
            try {
                if (!hargaStr.isEmpty()) harga = Long.parseLong(hargaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            // Tambahkan ke buffer
            tambahanBuffer.add(new PesananHolder.TambahanModel(nama, harga));
            // Perbarui tampilan daftar tambahan di TextView
            StringBuilder sb = new StringBuilder();
            for (PesananHolder.TambahanModel t : tambahanBuffer) {
                sb.append(t.getNama()).append(t.getHarga()>0 ? " (+Rp"+t.getHarga()+")" : " (Free)").append(", ");
            }
            tvTambahanList.setText("Tambahan: " + sb.toString().trim());
            // Kosongkan input tambahan
            etTambahanNama.setText("");
            etTambahanHarga.setText("");
        });

        // ═══════════════════════════════════════════════
        // Tombol "Simpan Menu"
        // ═══════════════════════════════════════════════
        btnSimpan.setOnClickListener(v -> {
            if (spinnerTenant.getSelectedItemPosition() < 0) {
                Toast.makeText(this, "Pilih tenant", Toast.LENGTH_SHORT).show();
                return;
            }
            // Ambil objek tenant yang dipilih berdasarkan posisi spinner
            TenantModel selectedTenant = tenantList.get(spinnerTenant.getSelectedItemPosition());
            // Baca teks input
            String nama = etNama.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();
            String gambar = etGambar.getText().toString().trim();

            if (nama.isEmpty() || hargaStr.isEmpty()) {
                Toast.makeText(this, "Nama dan harga harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            long harga;
            try {
                harga = Long.parseLong(hargaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Buat ID menu unik: tenantId + "_M" + 4 karakter acak
            String menuId = selectedTenant.getId() + "_M" + UUID.randomUUID().toString().substring(0,4).toUpperCase();

            // Buat objek MenuModel dan isi datanya
            MenuModel menu = new MenuModel();
            menu.setMenuId(menuId);
            menu.setNama(nama);
            menu.setDeskripsi(deskripsi);
            menu.setHarga(harga);
            menu.setGambar(gambar);
            menu.setTenantId(selectedTenant.getId());
            menu.setTambahan(tambahanBuffer);   // Simpan seluruh daftar tambahan (jika ada)

            // Simpan ke Firebase (node "menu" / menuId)
            FirebaseDatabase.getInstance().getReference("menu").child(menuId).setValue(menu)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        finish(); // Tutup activity, kembali ke MenuManagementActivity
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show());
        });
    }
}