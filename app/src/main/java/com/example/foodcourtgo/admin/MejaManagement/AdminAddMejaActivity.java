package com.example.foodcourtgo.admin.MejaManagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.MejaModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminAddMejaActivity extends AppCompatActivity {

    private EditText etNomor, etLokasi, etStatus;
    private Button btnSimpan;
    private DatabaseReference mejaRef;
    private String editId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_admin_add_meja);

        etNomor = findViewById(R.id.et_meja_nomor);
        etLokasi = findViewById(R.id.et_meja_lokasi);
        etStatus = findViewById(R.id.et_meja_status);
        btnSimpan = findViewById(R.id.btn_simpan_meja);
        mejaRef = FirebaseDatabase.getInstance().getReference("meja");

        // Cek apakah mode edit
        if (getIntent().hasExtra("mejaId")) {
            editId = getIntent().getStringExtra("mejaId");
            etNomor.setText(String.valueOf(getIntent().getIntExtra("nomor", 0)));
            etLokasi.setText(getIntent().getStringExtra("lokasi"));
            etStatus.setText(getIntent().getStringExtra("status"));
            btnSimpan.setText("Update Meja");
        }

        findViewById(R.id.btn_back_add_meja).setOnClickListener(v -> finish());

        btnSimpan.setOnClickListener(v -> {
            String nomorStr = etNomor.getText().toString().trim();
            String lokasi = etLokasi.getText().toString().trim();
            String status = etStatus.getText().toString().trim();

            if (nomorStr.isEmpty() || lokasi.isEmpty()) {
                Toast.makeText(this, "Nomor meja dan lokasi wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            int nomor = Integer.parseInt(nomorStr);
            if (status.isEmpty()) status = "available";

            if (editId != null) {
                // Update
                mejaRef.child(editId).child("nomor").setValue(nomor);
                mejaRef.child(editId).child("lokasi").setValue(lokasi);
                mejaRef.child(editId).child("status").setValue(status);
                Toast.makeText(this, "Meja diupdate", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Buat final copy untuk lambda
                final String finalLokasi = lokasi;
                final String finalStatus = status;
                final int finalNomor = nomor;
                // Tambah baru, buat ID otomatis: M001, M002, ...
                mejaRef.orderByKey().limitToLast(1).get()
                        .addOnSuccessListener(snapshot -> {
                            int newId = 1;
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String lastKey = child.getKey();
                                if (lastKey != null && lastKey.startsWith("M")) {
                                    try {
                                        newId = Integer.parseInt(lastKey.substring(1)) + 1;
                                    } catch (NumberFormatException e) { newId = 1; }
                                }
                            }
                            String newKey = String.format("M%03d", newId);
                            String qrString = "MEJA_" + finalNomor + "_" + System.currentTimeMillis();
                            MejaModel meja = new MejaModel(newKey, finalNomor, finalLokasi, qrString, finalStatus);
                            mejaRef.child(newKey).setValue(meja)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(AdminAddMejaActivity.this, "Meja ditambahkan", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        });
            }
        });
    }
}