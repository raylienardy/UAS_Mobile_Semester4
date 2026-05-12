package com.example.foodcourtgo.users;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.login.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import androidx.annotation.NonNull;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvUserId;
    private EditText etName, etUsername, etEmail, etPhone;
    private ProgressBar progressBar;
    private String userId;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_profile);

        ivBack = findViewById(R.id.ivBack);
        tvUserId = findViewById(R.id.tvUserId);
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        ivBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "Silakan login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvUserId.setText("ID: " + userId);
        userRef = FirebaseDatabase.getInstance().getReference("akun").child(userId);
        loadProfile();
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("noHp").getValue(String.class);
                    etName.setText(name != null ? name : "");
                    etUsername.setText(username != null ? username : "");
                    etEmail.setText(email != null ? email : "");
                    etPhone.setText(phone != null ? phone : "");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserProfileActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama, Username, dan Email wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("username", username);
        updates.put("email", email);
        updates.put("noHp", phone);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                // Update nama di SharedPreferences
                SharedPreferences prefs = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
                prefs.edit().putString("namaUser", name).apply();
                finish();
            } else {
                Toast.makeText(this, "Gagal menyimpan: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE).edit().clear().apply();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}