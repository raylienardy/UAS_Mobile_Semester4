package com.example.foodcourtgo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.admin.Pesanan.PesananActivity;

public class StatusPesananActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_pesanan);

        // Tombol kembali
        findViewById(R.id.btn_back_status).setOnClickListener(v -> finish());

        // Tombol kembali ke pesanan
        findViewById(R.id.btn_status_back_to_orders).setOnClickListener(v -> {
            startActivity(new Intent(this, PesananActivity.class));
            finish();
        });
    }
}