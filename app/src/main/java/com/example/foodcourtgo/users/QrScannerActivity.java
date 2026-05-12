package com.example.foodcourtgo.users;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.users.menu.DetailTenantActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private String tenantId, tenantNama, tenantGambar, tenantKategori, tenantDeskripsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_qr_scanner);

        // Ambil data tenant dari Intent
        tenantId = getIntent().getStringExtra("tenantId");
        tenantNama = getIntent().getStringExtra("tenantNama");
        tenantGambar = getIntent().getStringExtra("tenantGambar");
        tenantKategori = getIntent().getStringExtra("tenantKategori");
        tenantDeskripsi = getIntent().getStringExtra("tenantDeskripsi");

        // Cek izin kamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startScan();
        }
    }

    private void startScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Arahkan kamera ke QR Code meja");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        // Hapus baris berikut karena CustomScannerActivity tidak ada
        options.setCaptureActivity(CustomScannerActivity.class);
        barcodeLauncher.launch(options);
    }

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String scannedData = result.getContents();
                    // Validasi QR ke Firebase node "meja"
                    validasiMeja(scannedData);
                }
            });

    private void validasiMeja(String scannedCode) {
        // Asumsikan node "meja" memiliki child dengan key = kode QR, misal "M001"
        FirebaseDatabase.getInstance().getReference("meja")
                .child(scannedCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // QR valid, lanjut ke DetailTenantActivity
                            String mejaId = scannedCode;
                            Intent intent = new Intent(QrScannerActivity.this, DetailTenantActivity.class);
                            intent.putExtra("tenantId", tenantId);
                            intent.putExtra("tenantNama", tenantNama);
                            intent.putExtra("tenantGambar", tenantGambar);
                            intent.putExtra("tenantKategori", tenantKategori);
                            intent.putExtra("tenantDeskripsi", tenantDeskripsi);
                            intent.putExtra("mejaId", mejaId); // kirim mejaId untuk dicatat di pesanan nanti
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(QrScannerActivity.this, "QR Meja tidak dikenali", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QrScannerActivity.this, "Gagal validasi QR", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk scan QR", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}