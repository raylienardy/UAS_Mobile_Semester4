package com.example.foodcourtgo.admin.TenantManagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.TenantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminAddTenantActivity extends AppCompatActivity {

    private EditText etNama, etDeskripsi, etGambar;
    private Spinner spinnerKategori;
    private Button btnSimpan;
    private DatabaseReference tenantRef;
    private List<String> kategoriList = new ArrayList<>();
    private ArrayAdapter<String> kategoriAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_admin_add_tenant);

        ImageView ivBack = findViewById(R.id.ivBack);
        etNama = findViewById(R.id.etNama);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        etGambar = findViewById(R.id.etGambar);
        btnSimpan = findViewById(R.id.btnSimpan);

        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        // Load kategori dari Firebase
        DatabaseReference kategoriRef = FirebaseDatabase.getInstance().getReference("kategori");
        kategoriRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                kategoriList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String nama = snap.child("nama").getValue(String.class);
                    if (nama != null) kategoriList.add(nama);
                }
                kategoriAdapter = new ArrayAdapter<>(AdminAddTenantActivity.this,
                        android.R.layout.simple_spinner_item, kategoriList);
                kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerKategori.setAdapter(kategoriAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        ivBack.setOnClickListener(v -> finish());

        btnSimpan.setOnClickListener(v -> {
            String nama = etNama.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String kategori = spinnerKategori.getSelectedItem().toString();
            String gambar = etGambar.getText().toString().trim();

            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tenant harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

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
                            } catch (NumberFormatException e) {}
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