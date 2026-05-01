package com.example.foodcourtgo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminAddTenantActivity extends AppCompatActivity {

    private EditText etNama, etDeskripsi, etKategori, etGambar;
    private Button btnSimpan;
    private DatabaseReference tenantRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_tenant);

        ImageView ivBack = findViewById(R.id.ivBack);
        etNama = findViewById(R.id.etNama);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        etKategori = findViewById(R.id.etKategori);
        etGambar = findViewById(R.id.etGambar);
        btnSimpan = findViewById(R.id.btnSimpan);
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        ivBack.setOnClickListener(v -> finish());

        btnSimpan.setOnClickListener(v -> {
            String nama = etNama.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String kategori = etKategori.getText().toString().trim();
            String gambar = etGambar.getText().toString().trim();

            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tenant harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cari ID terakhir
            tenantRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int nextNumber = 1;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String key = data.getKey();
                        if (key != null && key.startsWith("T")) {
                            try {
                                int num = Integer.parseInt(key.substring(1));
                                nextNumber = num + 1;
                            } catch (NumberFormatException e) { /* ignore */ }
                        }
                    }
                    String newId = "T" + String.format("%04d", nextNumber);

                    TenantModel tenant = new TenantModel();
                    tenant.setId(newId);
                    tenant.setNama(nama);
                    tenant.setDeskripsi(deskripsi);
                    tenant.setKategori(kategori);
                    tenant.setGambar(gambar.isEmpty() ? "" : gambar);
                    tenant.setStatus("active");

                    tenantRef.child(newId).setValue(tenant)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(AdminAddTenantActivity.this, "Tenant berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AdminAddTenantActivity.this, "Gagal menambahkan tenant", Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AdminAddTenantActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}