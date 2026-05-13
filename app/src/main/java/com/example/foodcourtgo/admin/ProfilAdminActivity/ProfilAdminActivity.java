package com.example.foodcourtgo.admin.ProfilAdminActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.admin.MejaManagement.MejaManagementActivity;
import com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.TenantManagement.TenantManagementActivity;
import com.example.foodcourtgo.login.LoginActivity;

public class ProfilAdminActivity extends AppCompatActivity {

    // ── View untuk profil ──────────────────────────
    private TextView tvName, tvRole;                // Nama & role di card utama
    private TextView valueName, valueEmail, valuePhone;  // Info akun (nama, email, telepon)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_profil_admin); // Layout profil admin

        // ── Inisialisasi semua TextView ──────────────
        tvName      = findViewById(R.id.tv_profile_name);
        tvRole      = findViewById(R.id.tv_profile_role);
        valueName   = findViewById(R.id.value_profile_name);
        valueEmail  = findViewById(R.id.value_profile_email);
        valuePhone  = findViewById(R.id.value_profile_phone);

        // ── Ambil data user dari SharedPreferences ──
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        String nama  = pref.getString("namaUser", "Admin FoodCourt");
        String email = pref.getString("email", "admin@foodcourtgo.com");
        String role  = pref.getString("role", "super_admin");

        // ── Tampilkan data di card utama ─────────────
        tvName.setText(nama);
        tvRole.setText(role.equals("super_admin") ? "Super Admin" : "Admin");

        // ── Tampilkan data di card informasi akun ────
        valueName.setText("Nama: " + nama);
        valueEmail.setText("Email: " + email);
        valuePhone.setText("Telepon: 0812-3456-7890"); // Masih diisi manual

        // ── Tombol Edit Profil (belum ada aksi) ──────
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            // Arahkan ke activity edit profil (akan dikembangkan nanti)
            // Untuk sementara kosong
        });

        // ── Tombol Logout ────────────────────────────
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            // Hapus semua data login di SharedPreferences
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();

            // Arahkan ke halaman LoginActivity dan tutup semua activity sebelumnya
            Intent intent = new Intent(ProfilAdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // tutup activity ini
        });

        // ══════════════════════════════════════════════
        // Bottom Navigation
        // ══════════════════════════════════════════════
        findViewById(R.id.nav_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v ->
                startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v ->
                startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v ->
                startActivity(new Intent(this, PesananActivity.class)));
        findViewById(R.id.nav_profil).setOnClickListener(v -> {}); // sudah di halaman ini

        // Tombol "Meja" → ke halaman manajemen meja
        findViewById(R.id.btn_quick_meja).setOnClickListener(v ->
                startActivity(new Intent(this, MejaManagementActivity.class)));
    }
}