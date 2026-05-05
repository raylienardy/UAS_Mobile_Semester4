package com.example.foodcourtgo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class TenantProfileActivity extends AppCompatActivity {
    EditText etName, etEmail, etPhone, etLocation;
    Button btnSave;
    String tenantId;
    DatabaseReference tenantRef;
    ValueEventListener profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_profile);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        etName = findViewById(R.id.et_profile_stand_name);
        etEmail = findViewById(R.id.et_profile_email);
        etPhone = findViewById(R.id.et_profile_phone);
        etLocation = findViewById(R.id.et_profile_location);
        btnSave = findViewById(R.id.btn_save_profile);

        tenantRef = FirebaseDatabase.getInstance().getReference("tenant").child(tenantId);
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                TenantModel t = snap.getValue(TenantModel.class);
                if (t != null) {
                    etName.setText(t.getNama());
                    etEmail.setText(t.getEmail());
                    etPhone.setText(t.getTelepon());
                    etLocation.setText(t.getLokasi());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {}
        };
        tenantRef.addValueEventListener(profileListener);

        btnSave.setOnClickListener(v -> {
            String nama = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String lokasi = etLocation.getText().toString().trim();
            tenantRef.child("nama").setValue(nama);
            tenantRef.child("email").setValue(email);
            tenantRef.child("telepon").setValue(phone);
            tenantRef.child("lokasi").setValue(lokasi);
            Toast.makeText(this, "Profil disimpan", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) tenantRef.removeEventListener(profileListener);
    }
}