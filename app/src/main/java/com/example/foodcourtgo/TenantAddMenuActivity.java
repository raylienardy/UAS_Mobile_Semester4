package com.example.foodcourtgo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity.MenuModel;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class TenantAddMenuActivity extends AppCompatActivity {
    EditText etName, etPrice;
    Spinner spinnerKategori;
    TextView btnSimpan;
    String tenantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_add_menu);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        etName = findViewById(R.id.et_menu_name);
        etPrice = findViewById(R.id.et_menu_price);
        btnSimpan = findViewById(R.id.btn_save_menu);
        spinnerKategori = findViewById(R.id.spinner_kategori);

        ArrayAdapter<CharSequence> adapterKategori = ArrayAdapter.createFromResource(this,
                R.array.kategori_array, android.R.layout.simple_spinner_item);
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(adapterKategori);

        findViewById(R.id.btn_back_add_menu).setOnClickListener(v -> finish());

        btnSimpan.setOnClickListener(v -> {
            String nama = etName.getText().toString().trim();
            String hargaStr = etPrice.getText().toString().trim();
            if (nama.isEmpty() || hargaStr.isEmpty()) {
                Toast.makeText(this, "Nama dan harga wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            long harga;
            try {
                harga = Long.parseLong(hargaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            String kategori = spinnerKategori.getSelectedItem().toString();

            String menuId = tenantId + "_M" + System.currentTimeMillis() % 100000;
            MenuModel menu = new MenuModel();
            menu.setMenuId(menuId);
            menu.setNama(nama);
            menu.setKategori(kategori);
            menu.setHarga(harga);
            menu.setTenantId(tenantId);
            menu.setTambahan(new ArrayList<>());

            FirebaseDatabase.getInstance().getReference("menu").child(menuId)
                    .setValue(menu)
                    .addOnSuccessListener(u -> {
                        Toast.makeText(this, "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal menambahkan menu", Toast.LENGTH_SHORT).show());
        });
    }
}