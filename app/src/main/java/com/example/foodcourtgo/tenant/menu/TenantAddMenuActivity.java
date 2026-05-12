package com.example.foodcourtgo.tenant.menu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.MenuModel;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class TenantAddMenuActivity extends AppCompatActivity {

    // ── Form input ───────────────────────────────
    EditText etName, etPrice;          // Input nama dan harga menu
    Spinner spinnerKategori;          // Pilihan kategori menu
    TextView btnSimpan;               // Tombol simpan (TextView yang di-styling sebagai button)
    String tenantId;                  // ID tenant yang sedang login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_add_menu); // Layout form tambah menu tenant

        // ── Ambil tenantId dari SharedPreferences ──
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        // ── Inisialisasi view ──────────────────────
        etName = findViewById(R.id.et_menu_name);
        etPrice = findViewById(R.id.et_menu_price);
        btnSimpan = findViewById(R.id.btn_save_menu);
        spinnerKategori = findViewById(R.id.spinner_kategori);

        // ── Isi data spinner kategori dari array resource ──
        ArrayAdapter<CharSequence> adapterKategori = ArrayAdapter.createFromResource(this,
                R.array.kategori_array, android.R.layout.simple_spinner_item);
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(adapterKategori);

        // ── Tombol kembali di toolbar ──────────────
        findViewById(R.id.btn_back_add_menu).setOnClickListener(v -> finish());

        // ── Klik tombol simpan ──────────────────────
        btnSimpan.setOnClickListener(v -> {
            // Ambil teks dari input, hapus spasi di ujung
            String nama = etName.getText().toString().trim();
            String hargaStr = etPrice.getText().toString().trim();

            // Validasi: nama dan harga tidak boleh kosong
            if (nama.isEmpty() || hargaStr.isEmpty()) {
                Toast.makeText(this, "Nama dan harga wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parsing harga ke long
            long harga;
            try {
                harga = Long.parseLong(hargaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ambil kategori yang dipilih di spinner
            String kategori = spinnerKategori.getSelectedItem().toString();

            // Buat ID menu unik: tenantId + "_M" + 5 digit angka waktu
            String menuId = tenantId + "_M" + System.currentTimeMillis() % 100000;

            // Buat objek MenuModel dan isi data
            MenuModel menu = new MenuModel();
            menu.setMenuId(menuId);
            menu.setNama(nama);
            menu.setKategori(kategori);
            menu.setHarga(harga);
            menu.setTenantId(tenantId);
            menu.setTambahan(new ArrayList<>()); // Opsi tambahan kosong dulu

            // Simpan ke Firebase (node "menu" / menuId)
            FirebaseDatabase.getInstance().getReference("menu").child(menuId)
                    .setValue(menu)
                    .addOnSuccessListener(u -> {
                        Toast.makeText(this, "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        finish(); // Kembali ke daftar menu
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Gagal menambahkan menu", Toast.LENGTH_SHORT).show());
        });
    }
}