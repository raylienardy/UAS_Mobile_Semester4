package com.example.foodcourtgo.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import com.example.foodcourtgo.R;

public class MainActivity extends AppCompatActivity {

//  membuat variabel untuk nanti nangkap fitur
    Button btnSignup;
    TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        terhubung dengan activity_main.xml
        setContentView(R.layout.login_activity_main);


//      mengambil/nangkap tombol untuk login atau membuat akun
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

//        pindah halmaan ke SignUpActivity ketika tombol di klik
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });

//        pindah halaman ke LoginActivity ketika tombol di klik
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}