package com.example.foodcourtgo.tenant.profil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.tenant.menu.TenantMenuActivity;
import com.example.foodcourtgo.tenant.pesanan.TenantOrdersActivity;
import com.example.foodcourtgo.login.LoginActivity;
import com.example.foodcourtgo.model.TenantModel;
import com.example.foodcourtgo.tenant.dashboard.TenantDashboardActivity;
import com.google.firebase.database.*;

public class TenantProfileActivity extends AppCompatActivity {

    // ── Form input profil ───────────────────────────
    EditText etName, etEmail, etPhone, etLocation;

    // ── Tombol aksi ──────────────────────────────────
    TextView btnSave, btnLogout;               // TextView yang distyling sebagai tombol

    // ── Data tenant ──────────────────────────────────
    String tenantId;                           // ID tenant yang login
    DatabaseReference tenantRef;               // Referensi ke node "tenant/{tenantId}"
    ValueEventListener profileListener;        // Listener realtime untuk profil

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_profile); // Layout profil tenant

        // ── Ambil tenantId dari SharedPreferences ──
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        // ── Inisialisasi semua view ─────────────────
        etName     = findViewById(R.id.et_profile_stand_name);
        etEmail    = findViewById(R.id.et_profile_email);
        etPhone    = findViewById(R.id.et_profile_phone);
        etLocation = findViewById(R.id.et_profile_location);
        btnSave    = findViewById(R.id.btn_save_profile);
        btnLogout  = findViewById(R.id.btn_logout);

        // ── Ambil data profil dari Firebase ────────
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant").child(tenantId);
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                TenantModel t = snap.getValue(TenantModel.class);
                if (t != null) {
                    // Isi form dengan data dari Firebase
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

        // ── Tombol simpan profil ────────────────────
        btnSave.setOnClickListener(v -> {
            String nama    = etName.getText().toString().trim();
            String email   = etEmail.getText().toString().trim();
            String phone   = etPhone.getText().toString().trim();
            String lokasi  = etLocation.getText().toString().trim();

            // Simpan perubahan ke Firebase (hanya child yang diperbarui)
            tenantRef.child("nama").setValue(nama);
            tenantRef.child("email").setValue(email);
            tenantRef.child("telepon").setValue(phone);
            tenantRef.child("lokasi").setValue(lokasi);
            Toast.makeText(this, "Profil disimpan", Toast.LENGTH_SHORT).show();
        });

        // ── Tombol logout ───────────────────────────
        btnLogout.setOnClickListener(v -> {
            // Hapus semua data login dari SharedPreferences
            getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE)
                    .edit().clear().apply();
            // Arahkan ke LoginActivity dan hapus semua activity sebelumnya
            Intent intent = new Intent(TenantProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Tutup activity ini
        });

        // ── Tombol kembali di toolbar ────────────────
        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());

        // ══════════════════════════════════════════════
        // Bottom Navigation Tenant
        // ══════════════════════════════════════════════
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, TenantDashboardActivity.class)));
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v ->
                startActivity(new Intent(this, TenantMenuActivity.class)));
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v -> {}); // Halaman ini
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Lepaskan listener Firebase saat activity dihancurkan
        if (profileListener != null) tenantRef.removeEventListener(profileListener);
    }
}