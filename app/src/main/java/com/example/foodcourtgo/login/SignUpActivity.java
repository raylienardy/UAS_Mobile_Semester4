package com.example.foodcourtgo.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodcourtgo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

//    membuat variabel untuk nanti ditangkap
    private Button btnGoogle, btnCreateAccount;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_sign_up);

//        menangkap berbagai fitu seperti text atau tombol
        btnGoogle = findViewById(R.id.btnGoogle);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLogin = findViewById(R.id.tvLogin);

//        ini rencana nya ketika di ketik ada opsi login sebagai gmail
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Sign up with Google
            }
        });

//        untuk membuat akun setelah input di form
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                menangkap value dari form
                String name = ((EditText) findViewById(R.id.etName)).getText().toString().trim();
                String username = ((EditText) findViewById(R.id.etUsername)).getText().toString().trim();
                String email = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
                String password = ((EditText) findViewById(R.id.etPassword)).getText().toString().trim();

//                cek apakah semua input sudah diisi atau belum
                if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

//                untuk menangkap database dengan nama tabel "akun"
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("akun");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int jumlahAkun = (int) snapshot.getChildrenCount();
                        String idBaru = "A" + String.format("%04d", jumlahAkun + 1);

//                        tempat dimana akan menampung datanya nanti dalam bentuk object
                        HashMap<String, Object> data = new HashMap<>();

//                        input data ke dalam object
                        data.put("name", name);
                        data.put("username", username);
                        data.put("email", email);
                        data.put("pass", password);

//                        input ke database akun baru nya (dalam bentuk object)
                        ref.child(idBaru).setValue(data);

//                        info kalau akun sudah dibuat
                        Toast.makeText(SignUpActivity.this, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show();

//                        pindah halaman ke LoginActivity setelah membuat akun berhasil
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        finish();
                    }

//                    jaga2 kalau ada error
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SignUpActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

//        kalau mau pindah ke halaman login (semisal tidak jadi membuat akun)
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish(); // jika ingin menutup SignUpActivity
            }
        });
    }
}