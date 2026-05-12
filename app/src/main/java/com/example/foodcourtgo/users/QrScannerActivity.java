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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_qr_scanner);

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
        // Jangan pakai setCaptureActivity jika kelasnya tidak ada
        barcodeLauncher.launch(options);
    }

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    validasiMeja(result.getContents());
                }
            });

    private void validasiMeja(String scannedCode) {
        FirebaseDatabase.getInstance().getReference("meja")
                .child(scannedCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // QR valid, kirim ke HomeActivity dengan mode DINE_IN dan mejaId
                            Intent intent = new Intent(QrScannerActivity.this, HomeActivity.class);
                            intent.putExtra("orderMode", "DINE_IN");
                            intent.putExtra("mejaId", scannedCode);
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