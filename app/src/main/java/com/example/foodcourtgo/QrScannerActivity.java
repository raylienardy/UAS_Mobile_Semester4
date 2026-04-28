package com.example.foodcourtgo;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class QrScannerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        Button btnSelesai = findViewById(R.id.btnSelesai);
        btnSelesai.setOnClickListener(v -> finish());
    }
}