package com.example.foodcourtgo.tenant.profil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.tenant.menu.TenantMenuActivity;
import com.example.foodcourtgo.tenant.pesanan.TenantOrdersActivity;
import com.example.foodcourtgo.login.LoginActivity;
import com.example.foodcourtgo.model.TenantModel;
import com.example.foodcourtgo.tenant.dashboard.TenantDashboardActivity;
import com.google.firebase.database.*;

public class TenantProfileActivity extends AppCompatActivity {

    // Form input profil (lokasi diubah jadi TextView)
    EditText etName, etEmail, etPhone;
    TextView tvLocation;  // Ganti dari etLocation

    TextView btnSave, btnLogout;

    String tenantId;
    DatabaseReference tenantRef;
    ValueEventListener profileListener;

    // Untuk menyimpan data lama sebelum edit (opsional)
    String currentPassword; // asumsikan ada field password di node users atau tenant

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_profile);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        etName = findViewById(R.id.et_profile_stand_name);
        etEmail = findViewById(R.id.et_profile_email);
        etPhone = findViewById(R.id.et_profile_phone);
        tvLocation = findViewById(R.id.tv_profile_location);  // TextView
        btnSave = findViewById(R.id.btn_save_profile);
        btnLogout = findViewById(R.id.btn_logout);

        tenantRef = FirebaseDatabase.getInstance().getReference("tenant").child(tenantId);
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                TenantModel t = snap.getValue(TenantModel.class);
                if (t != null) {
                    etName.setText(t.getNama());
                    etEmail.setText(t.getEmail());
                    etPhone.setText(t.getTelepon());
                    tvLocation.setText(t.getLokasi()); // set ke TextView
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {}
        };
        tenantRef.addValueEventListener(profileListener);

        // Tombol simpan dengan konfirmasi password (akan diimplementasikan di nomor 2 nanti)
        btnSave.setOnClickListener(v -> {
            // Untuk nomor 1, kita hanya simpan tanpa password dulu, tapi lokasi tidak diikutkan
            String nama = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // Update hanya field yang boleh diubah (lokasi TIDAK diupdate)
            tenantRef.child("nama").setValue(nama);
            tenantRef.child("email").setValue(email);
            tenantRef.child("telepon").setValue(phone);
            // Lokasi tidak disimpan karena readonly

            Toast.makeText(this, "Profil disimpan", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(TenantProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());

        // Bottom Navigation
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, TenantDashboardActivity.class)));
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v ->
                startActivity(new Intent(this, TenantMenuActivity.class)));
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v -> {});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) tenantRef.removeEventListener(profileListener);
    }
}