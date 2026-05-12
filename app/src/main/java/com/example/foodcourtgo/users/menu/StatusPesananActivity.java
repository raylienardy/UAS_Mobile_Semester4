package com.example.foodcourtgo.users.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.ItemPesananModel;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.example.foodcourtgo.users.HomeActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StatusPesananActivity extends AppCompatActivity {

    private TextView tvOrderId, tvMeja, tvWaktu, tvStatus, tvTotalHarga;
    private LinearLayout llTimeline, llItemsContainer;
    private Button btnBackToHome;

    private String pesananId;
    private PesananAdminModel pesanan;

    private ValueEventListener pesananListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_status_pesanan);

        // Inisialisasi view
        tvOrderId = findViewById(R.id.tvOrderId);
        tvMeja = findViewById(R.id.tvMeja);
        tvWaktu = findViewById(R.id.tvWaktu);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotalHarga = findViewById(R.id.tvTotalHarga);
        llTimeline = findViewById(R.id.llTimeline);
        llItemsContainer = findViewById(R.id.llItemsContainer);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        // Ambil pesananId dari Intent
        pesananId = getIntent().getStringExtra("pesananId");
        if (pesananId == null || pesananId.isEmpty()) {
            Toast.makeText(this, "ID Pesanan tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Muat data pesanan dari Firebase secara realtime
        muatPesanan();

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(StatusPesananActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void muatPesanan() {
        pesananListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(StatusPesananActivity.this, "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                pesanan = snapshot.getValue(PesananAdminModel.class);
                if (pesanan != null) {
                    tampilkanDataPesanan();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StatusPesananActivity.this, "Gagal memuat status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        FirebaseDatabase.getInstance().getReference("pesanan")
                .child(pesananId)
                .addValueEventListener(pesananListener);
    }

    private void tampilkanDataPesanan() {
        // Header
        tvOrderId.setText("#" + pesanan.getId());
        tvMeja.setText("Meja: " + (pesanan.getMeja() != null ? pesanan.getMeja() : "Take Away"));
        tvWaktu.setText("Waktu: " + (pesanan.getWaktu() != null ? pesanan.getWaktu() : "-"));
        tvTotalHarga.setText(formatRupiah(pesanan.getTotalHarga()));

        // Status dan timeline
        String status = pesanan.getStatus();
        updateStatusUI(status);

        // Daftar item
        tampilkanItemPesanan(pesanan.getItems());
    }

    private void updateStatusUI(String status) {
        // Update chip status
        tvStatus.setText(getStatusText(status));
        int bgRes;
        switch (status) {
            case "pending":
                bgRes = R.drawable.bg_chip_pending;
                break;
            case "processing":
                bgRes = R.drawable.bg_chip_process; // buat drawable ini
                break;
            case "done":
                bgRes = R.drawable.bg_chip_done;
                break;
            case "cancelled":
                bgRes = R.drawable.bg_chip_cancelled;
                break;
            default:
                bgRes = R.drawable.bg_chip_pending;
        }
        tvStatus.setBackgroundResource(bgRes);

        // Timeline sederhana (opsional: bisa pakai progress bar)
        llTimeline.removeAllViews();
        String[] steps = {"Menunggu Pembayaran", "Diproses", "Siap Diambil/Diantar", "Selesai"};
        int currentStep = 0;
        switch (status) {
            case "pending": currentStep = 0; break;
            case "processing": currentStep = 1; break;
            case "done": currentStep = 3; break;
            case "cancelled": currentStep = -1; break;
        }

        if (status.equals("cancelled")) {
            TextView tv = new TextView(this);
            tv.setText("Pesanan Dibatalkan");
            tv.setTextColor(getColor(R.color.red_500));
            tv.setPadding(0, 8, 0, 8);
            llTimeline.addView(tv);
        } else {
            for (int i = 0; i < steps.length; i++) {
                View stepView = getLayoutInflater().inflate(R.layout.item_timeline, llTimeline, false);
                TextView tvStep = stepView.findViewById(R.id.tvStep);
                View vBullet = stepView.findViewById(R.id.vBullet);
                tvStep.setText(steps[i]);
                if (i <= currentStep) {
                    tvStep.setTextColor(getColor(R.color.blue_700));
                    vBullet.setBackgroundResource(R.drawable.bg_bullet_active);
                } else {
                    tvStep.setTextColor(getColor(R.color.gray_400));
                    vBullet.setBackgroundResource(R.drawable.bg_bullet_inactive);
                }
                llTimeline.addView(stepView);
            }
        }
    }

    private void tampilkanItemPesanan(List<ItemPesananModel> items) {
        llItemsContainer.removeAllViews();
        if (items == null) return;

        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
        for (ItemPesananModel item : items) {
            View itemView = getLayoutInflater().inflate(R.layout.item_pesanan_status, llItemsContainer, false);
            TextView tvNama = itemView.findViewById(R.id.tvItemNama);
            TextView tvQty = itemView.findViewById(R.id.tvItemQty);
            TextView tvHarga = itemView.findViewById(R.id.tvItemHarga);

            tvNama.setText(item.getNama());
            tvQty.setText("x" + item.getQty());
            long subtotal = item.getHarga() * item.getQty() + item.getHargaTambahan();
            tvHarga.setText(formatRupiah(subtotal));

            llItemsContainer.addView(itemView);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Menunggu Pembayaran";
            case "processing": return "Sedang Diproses";
            case "done": return "Selesai";
            case "cancelled": return "Dibatalkan";
            default: return status;
        }
    }

    private String formatRupiah(long nominal) {
        return "Rp" + String.format("%,d", nominal).replace(',', '.');
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pesananListener != null) {
            FirebaseDatabase.getInstance().getReference("pesanan")
                    .child(pesananId).removeEventListener(pesananListener);
        }
    }
}