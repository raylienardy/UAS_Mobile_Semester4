package com.example.foodcourtgo.tenant.pesanan;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.addson.ItemPesananModel;
import com.example.foodcourtgo.addson.PesananAdminModel;
import com.google.firebase.database.*;

public class TenantOrderDetailActivity extends AppCompatActivity {

    // ── Komponen tampilan ────────────────────────
    TextView tvOrderNumber, tvTableCode, tvOrderTime, tvMenu1, tvMenu2, tvSubtotal, chipStatus;
    TextView btnProcess, btnCancel;                // Tombol aksi (Proses / Batalkan)

    // ── Data pesanan ─────────────────────────────
    String pesananId;                              // ID pesanan yang diterima dari halaman sebelumnya
    DatabaseReference pesananRef;                  // Referensi ke node spesifik "pesanan/{pesananId}"
    ValueEventListener detailListener;             // Listener untuk membaca data realtime
    AlertDialog statusDialog;                      // Dialog konfirmasi ubah status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_order_detail); // Layout detail pesanan tenant

        // ── Ambil ID pesanan dari Intent ──────────
        pesananId = getIntent().getStringExtra("pesananId");
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan").child(pesananId);

        // ── Inisialisasi semua view ───────────────
        tvOrderNumber = findViewById(R.id.tv_detail_order_number);
        tvTableCode   = findViewById(R.id.tv_detail_table_code);
        tvOrderTime   = findViewById(R.id.tv_detail_order_time);
        tvMenu1       = findViewById(R.id.tv_detail_item_1);
        tvMenu2       = findViewById(R.id.tv_detail_item_2);
        tvSubtotal    = findViewById(R.id.tv_detail_subtotal);
        chipStatus    = findViewById(R.id.chip_detail_order_status);

        btnProcess    = findViewById(R.id.btn_process_order_detail);
        btnCancel     = findViewById(R.id.btn_cancel_order_detail);

        // ── Tombol kembali di toolbar ─────────────
        findViewById(R.id.btn_back_order_detail).setOnClickListener(v -> finish());

        // ── Listener untuk memantau perubahan data pesanan ──
        detailListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                PesananAdminModel p = snap.getValue(PesananAdminModel.class);
                if (p == null) return;

                // Tampilkan informasi dasar pesanan
                tvOrderNumber.setText(p.getId());
                tvTableCode.setText("Meja / Kode: " + p.getMeja());
                tvOrderTime.setText("Waktu: " + p.getWaktu());
                chipStatus.setText(p.getStatus());

                // Tampilkan item pesanan (dua item pertama)
                if (p.getItems() != null && p.getItems().size() > 0) {
                    ItemPesananModel item1 = p.getItems().get(0);
                    tvMenu1.setText(item1.getNama() + "  " + item1.getQty() + "x  Rp " + item1.getHarga());
                } else tvMenu1.setText("");

                if (p.getItems() != null && p.getItems().size() > 1) {
                    ItemPesananModel item2 = p.getItems().get(1);
                    tvMenu2.setText(item2.getNama() + "  " + item2.getQty() + "x  Rp " + item2.getHarga());
                } else tvMenu2.setText("");

                // Subtotal
                tvSubtotal.setText("Subtotal Rp " + String.format("%,d", p.getTotalHarga()));

                // Perbarui tombol aksi sesuai status saat ini
                updateButtonState(p.getStatus());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Toast.makeText(TenantOrderDetailActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        };
        pesananRef.addValueEventListener(detailListener);
    }

    /**
     * Mengatur tampilan dan aksi tombol berdasarkan status pesanan.
     * - pending : tombol "Proses Pesanan" dan "Batalkan Pesanan"
     * - processing : tombol "Pesanan Selesai", tombol batal disembunyikan
     * - done : tombol "Pesanan Selesai" tidak bisa diklik, batal hilang
     * - cancelled : semua tombol aksi hilang
     */
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
                btnProcess.setClickable(false);   // tidak bisa diklik lagi
                btnProcess.setVisibility(android.view.View.VISIBLE);
                btnCancel.setVisibility(android.view.View.GONE);
                break;
            case "cancelled":
                btnProcess.setVisibility(android.view.View.GONE);
                btnCancel.setVisibility(android.view.View.GONE);
                break;
        }
    }

    /**
     * Menampilkan dialog konfirmasi, lalu mengubah status pesanan di Firebase.
     */
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
                    // Update child "status" pada node pesanan
                    pesananRef.child("status").setValue(newStatus)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(TenantOrderDetailActivity.this, "Status berhasil diubah", Toast.LENGTH_SHORT).show();
                                // Segera perbarui UI tanpa menunggu listener
                                updateButtonState(newStatus);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(TenantOrderDetailActivity.this, "Gagal mengubah status", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    // Tidak lakukan apa‑apa
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Tutup dialog jika masih terbuka
        if (statusDialog != null && statusDialog.isShowing()) {
            statusDialog.dismiss();
        }
        // Lepas listener Firebase untuk mencegah memory leak
        if (detailListener != null) pesananRef.removeEventListener(detailListener);
    }
}