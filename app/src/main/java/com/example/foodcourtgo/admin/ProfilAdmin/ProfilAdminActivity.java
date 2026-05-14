package com.example.foodcourtgo.admin.ProfilAdmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.login.LoginActivity;

public class ProfilAdminActivity extends AppCompatActivity {

    private TextView tvName, tvRole;
    private TextView valueName, valueEmail, valuePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_profil_admin);

        // Inisialisasi TextView
        tvName = findViewById(R.id.tv_profile_name);
        tvRole = findViewById(R.id.tv_profile_role);
        valueName = findViewById(R.id.value_profile_name);
        valueEmail = findViewById(R.id.value_profile_email);
        valuePhone = findViewById(R.id.value_profile_phone);

        // Ambil data user dari SharedPreferences
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        String nama = pref.getString("namaUser", "Admin FoodCourt");
        String email = pref.getString("email", "admin@foodcourtgo.com");
        String role = pref.getString("role", "super_admin");

        // Tampilkan data di card utama
        tvName.setText(nama);
        tvRole.setText(role.equals("super_admin") ? "Super Admin" : "Admin");

        // Tampilkan data di card informasi akun
        valueName.setText("Nama: " + nama);
        valueEmail.setText("Email: " + email);
        valuePhone.setText("Telepon: 0812-3456-7890");

        // Tombol Edit Profil (placeholder)
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            // Arahkan ke activity edit profil (belum dibuat)
        });

        // Tombol Logout
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfilAdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Tombol back ke Dashboard
        findViewById(R.id.btn_back_dashboard).setOnClickListener(v -> {
            Intent intent = new Intent(ProfilAdminActivity.this, DashboardAdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}