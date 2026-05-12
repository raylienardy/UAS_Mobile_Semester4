package com.example.foodcourtgo.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.users.HomeActivity;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.tenant.dashboard.TenantDashboardActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

//    membuat variabel untuk menampung nanti
    private Button btnGoogle, btnLogin;
    private TextView tvForgot, tvSignup;
    private EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_login);

//        menangkap fitu2 tombol atau teks yang ada di xml
        btnGoogle = findViewById(R.id.btnGoogle);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgot = findViewById(R.id.tvForgot);
        tvSignup = findViewById(R.id.tvSignup);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

//        untuk login di google
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google Login belum diimplementasikan", Toast.LENGTH_SHORT).show()
        );

//        untuk login dan pindah halaman (sebagai yang sudah login)
        btnLogin.setOnClickListener(v -> {
            String usernameOrEmail = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

//            cek apakah username atau password kosong
            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username/Email dan password harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

//            untuk mengambil database dari firebase dan ambil "akun"
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("akun");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

//                    sebagai tempat tampung dulu
                    boolean loginBerhasil = false;
                    String userId = "";
                    String namaUser = "";
                    String role = "";
                    String tenantId = "";

                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {

//                        ambil value dari database firebase
                        Object emailObj    = childSnapshot.child("email").getValue();
                        Object usernameObj = childSnapshot.child("username").getValue();
                        Object passObj     = childSnapshot.child("pass").getValue();
                        Object nameObj     = childSnapshot.child("name").getValue();
                        Object roleObj     = childSnapshot.child("role").getValue();
                        Object tenantIdObj = childSnapshot.child("tenantId").getValue();

//                        cek apakah kosong atau tidak
                        String email    = emailObj == null ? "" : emailObj.toString();
                        String username = usernameObj == null ? "" : usernameObj.toString();
                        String pass     = passObj == null ? "" : passObj.toString();
                        String name     = nameObj == null ? "" : nameObj.toString();
                        String roleStr  = roleObj == null ? "customer" : roleObj.toString();
                        String tenId    = tenantIdObj == null ? "" : tenantIdObj.toString();

//                        verifikasi login, jika email atau username sama dengan password
                        if ((usernameOrEmail.equalsIgnoreCase(email) ||
                                usernameOrEmail.equalsIgnoreCase(username))
                                && pass.equals(password)) {

                            loginBerhasil = true;
                            userId = childSnapshot.getKey();
                            namaUser = name;
                            role = roleStr;
                            tenantId = tenId;
                            break;
                        }
                    }

//                    jika login ternyata true (setelah verifikasi diatas)
                    if (loginBerhasil) {

//                        memasukan data tadi yang sudah sesuai ke variable tampungan tadi diatas
                        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("userId", userId);
                        editor.putString("namaUser", namaUser);
                        editor.putString("role", role);
                        if ("tenant".equals(role)) {
                            editor.putString("tenantId", tenantId);
                        }
                        editor.apply();

                        if ("super_admin".equals(role)) {
//                          jika ternyata yang login adalah admin maka masuk ke DashboardAdminActivity.java
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                        } else if ("tenant".equals(role)) {
//
                            startActivity(new Intent(LoginActivity.this, TenantDashboardActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }
                        finish();
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this,
                                "Username/Email atau password salah", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Fitur lupa password belum tersedia", Toast.LENGTH_SHORT).show()
        );

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });
    }
}