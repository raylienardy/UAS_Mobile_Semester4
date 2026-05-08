package com.example.foodcourtgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.login.LoginActivity;

public class ProfilAdminActivity extends AppCompatActivity {

    private TextView tvName, tvRole, valueName, valueEmail, valuePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_admin);

        tvName = findViewById(R.id.tv_profile_name);
        tvRole = findViewById(R.id.tv_profile_role);
        valueName = findViewById(R.id.value_profile_name);
        valueEmail = findViewById(R.id.value_profile_email);
        valuePhone = findViewById(R.id.value_profile_phone);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        String nama = pref.getString("namaUser", "Admin FoodCourt");
        String email = pref.getString("email", "admin@foodcourtgo.com");
        String role = pref.getString("role", "super_admin");

        tvName.setText(nama);
        tvRole.setText(role.equals("super_admin") ? "Super Admin" : "Admin");
        valueName.setText("Nama: " + nama);
        valueEmail.setText("Email: " + email);
        valuePhone.setText("Telepon: 0812-3456-7890");

        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            // Arahkan ke edit profil (nantikan)
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(ProfilAdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        findViewById(R.id.nav_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v ->
                startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v ->
                startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v ->
                startActivity(new Intent(this, PesananActivity.class)));
        findViewById(R.id.nav_profil).setOnClickListener(v -> {}); // sudah di halaman ini
    }

}