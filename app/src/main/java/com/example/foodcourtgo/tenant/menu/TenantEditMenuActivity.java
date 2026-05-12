package com.example.foodcourtgo.tenant.menu;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TenantEditMenuActivity extends AppCompatActivity {

    // ── Form input untuk edit menu ──────────────
    EditText etNama, etDeskripsi, etHarga, etGambar;

    // ID menu yang akan diedit, dikirim dari TenantMenuActivity
    String menuId;

    // Referensi ke node spesifik di Firebase
    DatabaseReference menuRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_edit_menu); // Layout edit menu

        // ── Ambil data yang dikirim dari TenantMenuActivity ──
        menuId = getIntent().getStringExtra("menuId");

        // Inisialisasi view
        etNama = findViewById(R.id.et_menu_name);
        etDeskripsi = findViewById(R.id.et_menu_deskripsi);
        etHarga = findViewById(R.id.et_menu_price);
        etGambar = findViewById(R.id.et_menu_gambar);

        // ── Isi form dengan data lama ───────────────
        etNama.setText(getIntent().getStringExtra("nama"));
        etDeskripsi.setText(getIntent().getStringExtra("deskripsi"));
        etHarga.setText(String.valueOf(getIntent().getLongExtra("harga", 0)));
        etGambar.setText(getIntent().getStringExtra("gambar"));

        // Referensi ke node menu/{menuId} di Firebase
        menuRef = FirebaseDatabase.getInstance().getReference("menu").child(menuId);

        // ── Tombol simpan perubahan ──────────────────
        findViewById(R.id.btn_save_menu).setOnClickListener(v -> {
            // Ambil nilai baru dari form
            String nama = etNama.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();
            String gambar = etGambar.getText().toString().trim();

            // Validasi: nama dan harga tidak boleh kosong
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

            // Update hanya child yang berubah di Firebase
            menuRef.child("nama").setValue(nama);
            menuRef.child("deskripsi").setValue(deskripsi);
            menuRef.child("harga").setValue(harga);
            menuRef.child("gambar").setValue(gambar);

            Toast.makeText(this, "Menu diperbarui", Toast.LENGTH_SHORT).show();
            finish(); // Kembali ke daftar menu
        });
    }
}