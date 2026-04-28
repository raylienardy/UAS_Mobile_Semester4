package com.example.foodcourtgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class TakeAwayDineInActivity extends AppCompatActivity {

    private ImageView ivBack;
    private Button btnTakeAway, btnDineIn;

    private String tenantId, tenantNama, tenantGambar, tenantKategori, tenantDeskripsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takeaway_dinein);

        ivBack = findViewById(R.id.ivBack);
        btnTakeAway = findViewById(R.id.btnTakeAway);
        btnDineIn = findViewById(R.id.btnDineIn);

        Intent intent = getIntent();
        tenantId = intent.getStringExtra("tenantId");
        tenantNama = intent.getStringExtra("tenantNama");
        tenantGambar = intent.getStringExtra("tenantGambar");
        tenantKategori = intent.getStringExtra("tenantKategori");
        tenantDeskripsi = intent.getStringExtra("tenantDeskripsi");

        ivBack.setOnClickListener(v -> finish());

        btnTakeAway.setOnClickListener(v -> {
            Intent menuIntent = new Intent(this, DetailTenantActivity.class);
            menuIntent.putExtra("tenantId", tenantId);
            menuIntent.putExtra("tenantNama", tenantNama);
            menuIntent.putExtra("tenantGambar", tenantGambar);
            menuIntent.putExtra("tenantKategori", tenantKategori);
            menuIntent.putExtra("tenantDeskripsi", tenantDeskripsi);
            startActivity(menuIntent);
        });

        btnDineIn.setOnClickListener(v -> {
            startActivity(new Intent(this, QrScannerActivity.class));
        });
    }
}