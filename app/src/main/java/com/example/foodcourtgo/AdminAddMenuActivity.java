package com.example.foodcourtgo;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminAddMenuActivity extends AppCompatActivity {

    private EditText etNama, etDeskripsi, etHarga, etGambar, etTambahanNama, etTambahanHarga;
    private Spinner spinnerTenant;
    private Button btnSimpan, btnTambahTambahan;
    private TextView tvTambahanList;
    private List<TenantModel> tenantList = new ArrayList<>();
    private List<TambahanModel> tambahanBuffer = new ArrayList<>();
    private ArrayAdapter<String> tenantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_menu);

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

        ivBack.setOnClickListener(v -> finish());

        // Load tenant spinner
        DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
        tenantAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTenant.setAdapter(tenantAdapter);

        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();
                List<String> names = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    TenantModel t = snap.getValue(TenantModel.class);
                    if (t != null) {
                        t.setId(snap.getKey());
                        tenantList.add(t);
                        names.add(t.getNama());
                    }
                }
                tenantAdapter.clear();
                tenantAdapter.addAll(names);
                tenantAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

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
            tambahanBuffer.add(new TambahanModel(nama, harga));
            StringBuilder sb = new StringBuilder();
            for (TambahanModel t : tambahanBuffer) {
                sb.append(t.getNama()).append(t.getHarga()>0 ? " (+Rp"+t.getHarga()+")" : " (Free)").append(", ");
            }
            tvTambahanList.setText("Tambahan: " + sb.toString().trim());
            etTambahanNama.setText("");
            etTambahanHarga.setText("");
        });

        btnSimpan.setOnClickListener(v -> {
            if (spinnerTenant.getSelectedItemPosition() < 0) {
                Toast.makeText(this, "Pilih tenant", Toast.LENGTH_SHORT).show();
                return;
            }
            TenantModel selectedTenant = tenantList.get(spinnerTenant.getSelectedItemPosition());
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

            String menuId = selectedTenant.getId() + "_M" + UUID.randomUUID().toString().substring(0,4).toUpperCase();
            MenuModel menu = new MenuModel();
            menu.setMenuId(menuId);
            menu.setNama(nama);
            menu.setDeskripsi(deskripsi);
            menu.setHarga(harga);
            menu.setGambar(gambar);
            menu.setTenantId(selectedTenant.getId());
            menu.setTambahan(tambahanBuffer);

            FirebaseDatabase.getInstance().getReference("menu").child(menuId).setValue(menu)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show());
        });
    }
}