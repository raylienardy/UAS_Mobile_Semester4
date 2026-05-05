package com.example.foodcourtgo;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TenantEditMenuActivity extends AppCompatActivity {
    EditText etNama, etDeskripsi, etHarga, etGambar;
    String menuId;
    DatabaseReference menuRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_edit_menu);

        menuId = getIntent().getStringExtra("menuId");
        etNama = findViewById(R.id.et_menu_name);
        etDeskripsi = findViewById(R.id.et_menu_deskripsi);
        etHarga = findViewById(R.id.et_menu_price);
        etGambar = findViewById(R.id.et_menu_gambar);

        etNama.setText(getIntent().getStringExtra("nama"));
        etDeskripsi.setText(getIntent().getStringExtra("deskripsi"));
        etHarga.setText(String.valueOf(getIntent().getLongExtra("harga", 0)));
        etGambar.setText(getIntent().getStringExtra("gambar"));

        menuRef = FirebaseDatabase.getInstance().getReference("menu").child(menuId);

        findViewById(R.id.btn_save_menu).setOnClickListener(v -> {
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

            menuRef.child("nama").setValue(nama);
            menuRef.child("deskripsi").setValue(deskripsi);
            menuRef.child("harga").setValue(harga);
            menuRef.child("gambar").setValue(gambar);
            Toast.makeText(this, "Menu diperbarui", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}