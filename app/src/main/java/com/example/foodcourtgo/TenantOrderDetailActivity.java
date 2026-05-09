package com.example.foodcourtgo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.addson_PaymentActivity_PesananActivity.ItemPesananModel;
import com.example.foodcourtgo.addson_PaymentActivity_PesananActivity.PesananAdminModel;
import com.google.firebase.database.*;

public class TenantOrderDetailActivity extends AppCompatActivity {
    TextView tvOrderNumber, tvTableCode, tvOrderTime, tvMenu1, tvMenu2, tvSubtotal, chipStatus;
    TextView btnProcess, btnCancel;
    String pesananId;
    DatabaseReference pesananRef;
    ValueEventListener detailListener;
    AlertDialog statusDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_order_detail);

        pesananId = getIntent().getStringExtra("pesananId");
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan").child(pesananId);

        tvOrderNumber = findViewById(R.id.tv_detail_order_number);
        tvTableCode = findViewById(R.id.tv_detail_table_code);
        tvOrderTime = findViewById(R.id.tv_detail_order_time);
        tvMenu1 = findViewById(R.id.tv_detail_item_1);
        tvMenu2 = findViewById(R.id.tv_detail_item_2);
        tvSubtotal = findViewById(R.id.tv_detail_subtotal);
        chipStatus = findViewById(R.id.chip_detail_order_status);

        btnProcess = findViewById(R.id.btn_process_order_detail);
        btnCancel = findViewById(R.id.btn_cancel_order_detail);

        findViewById(R.id.btn_back_order_detail).setOnClickListener(v -> finish());

        detailListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                PesananAdminModel p = snap.getValue(PesananAdminModel.class);
                if (p == null) return;

                tvOrderNumber.setText(p.getId());
                tvTableCode.setText("Meja / Kode: " + p.getMeja());
                tvOrderTime.setText("Waktu: " + p.getWaktu());
                chipStatus.setText(p.getStatus());

                // Tampilkan item
                if (p.getItems() != null && p.getItems().size() > 0) {
                    ItemPesananModel item1 = p.getItems().get(0);
                    tvMenu1.setText(item1.getNama() + "  " + item1.getQty() + "x  Rp " + item1.getHarga());
                } else tvMenu1.setText("");
                if (p.getItems() != null && p.getItems().size() > 1) {
                    ItemPesananModel item2 = p.getItems().get(1);
                    tvMenu2.setText(item2.getNama() + "  " + item2.getQty() + "x  Rp " + item2.getHarga());
                } else tvMenu2.setText("");

                tvSubtotal.setText("Subtotal Rp " + String.format("%,d", p.getTotalHarga()));

                // Perbarui tampilan tombol
                updateButtonState(p.getStatus());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Toast.makeText(TenantOrderDetailActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        };
        pesananRef.addValueEventListener(detailListener);
    }

    private void updateButtonState(String status) {
        switch (status) {
            case "pending":
                btnProcess.setText("Proses Pesanan");
                btnProcess.setOnClickListener(v -> updateStatus("processing"));
                btnProcess.setVisibility(android.view.View.VISIBLE);
                btnCancel.setText("Batalkan Pesanan");
                btnCancel.setOnClickListener(v -> updateStatus("cancelled"));
                btnCancel.setVisibility(android.view.View.VISIBLE);
                break;
            case "processing":
                btnProcess.setText("Pesanan Selesai");
                btnProcess.setOnClickListener(v -> updateStatus("done"));
                btnProcess.setVisibility(android.view.View.VISIBLE);
                btnCancel.setVisibility(android.view.View.GONE);
                break;
            case "done":
                btnProcess.setText("Pesanan Selesai");
                btnProcess.setClickable(false);
                btnProcess.setVisibility(android.view.View.VISIBLE);
                btnCancel.setVisibility(android.view.View.GONE);
                break;
            case "cancelled":
                btnProcess.setVisibility(android.view.View.GONE);
                btnCancel.setVisibility(android.view.View.GONE);
                break;
        }
    }

    private void updateStatus(String newStatus) {
        String message;
        if (newStatus.equals("processing")) {
            message = "Ubah status pesanan menjadi Diproses?";
        } else if (newStatus.equals("done")) {
            message = "Tandai pesanan sebagai Selesai?";
        } else {
            message = "Batalkan pesanan ini?";
        }

        statusDialog = new AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage(message)
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Update status di Firebase
                    pesananRef.child("status").setValue(newStatus)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(TenantOrderDetailActivity.this, "Status berhasil diubah", Toast.LENGTH_SHORT).show();
                                // Perbarui UI secara langsung (antisipasi listener lambat)
                                updateButtonState(newStatus);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(TenantOrderDetailActivity.this, "Gagal mengubah status", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    // Tutup dialog tanpa efek
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusDialog != null && statusDialog.isShowing()) {
            statusDialog.dismiss();
        }
        if (detailListener != null) pesananRef.removeEventListener(detailListener);
    }
}