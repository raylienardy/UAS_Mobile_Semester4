package com.example.foodcourtgo.users.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.model.NotificationModel;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.users.HomeActivity;
// Impor kelas PesananHolder dan PesananItem dari package DetailTenantActivity
// Ini adalah kelas pembantu untuk menyimpan dan membawa data pesanan antar activity
import com.example.foodcourtgo.model.PesananHolder;
import com.example.foodcourtgo.model.PesananItem;
import com.example.foodcourtgo.model.ItemPesananModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    // ── View dari layout ──────────────────────────
    private ImageView ivBack;                // Tombol kembali
    private LinearLayout llPesananList;      // Wadah untuk menampilkan item pesanan satu per satu
    private TextView tvTotalBayar;           // Teks total harga yang harus dibayar
    private Button btnBayar;                 // Tombol untuk melakukan pembayaran

    // ── Data pesanan ─────────────────────────────
    private List<PesananItem> pesananList;   // Daftar item pesanan yang dibawa dari halaman sebelumnya
    private long totalHarga;                 // Total harga keseluruhan
    private String tenantId;                 // ID tenant tempat pesan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan dengan layout XML khusus pembayaran
        setContentView(R.layout.users_menu_activity_payment);

        // ── Inisialisasi view ──────────────────────
        ivBack = findViewById(R.id.ivBack);
        llPesananList = findViewById(R.id.llPesananList);
        tvTotalBayar = findViewById(R.id.tvTotalBayar);
        btnBayar = findViewById(R.id.btnBayar);

        // Ambil tenantId yang dikirim dari DetailTenantActivity (untuk menyimpan pesanan nanti)
        tenantId = getIntent().getStringExtra("tenantId");

        // ── Ambil data pesanan dari PesananHolder (tempat penyimpanan global sementara) ──
        pesananList = PesananHolder.getPesananList();
        if (pesananList == null || pesananList.isEmpty()) {
            // Jika tidak ada pesanan, beri tahu dan tutup activity
            Toast.makeText(this, "Tidak ada pesanan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ── Loop untuk menampilkan setiap item pesanan dan menghitung total ──
        totalHarga = 0;
        for (PesananItem item : pesananList) {
            totalHarga += item.getTotalHarga();  // Tambahkan harga total item (harga dasar + tambahan)

            // Buat tampilan untuk satu item pesanan dari layout kecil item_pesanan.xml
            View itemView = getLayoutInflater().inflate(R.layout.item_pesanan, llPesananList, false);
            TextView tvNama = itemView.findViewById(R.id.tvPesananNama);
            TextView tvOpsi = itemView.findViewById(R.id.tvPesananOpsi);
            TextView tvHarga = itemView.findViewById(R.id.tvPesananHarga);

            // Isi teks dengan data pesanan
            tvNama.setText(item.getNama());
            // Jika ada opsi (misal "Level Pedas, Extra Keju"), tampilkan; jika tidak, sembunyikan
            if (item.getOpsi() != null && !item.getOpsi().isEmpty()) {
                tvOpsi.setText(item.getOpsi());
            } else {
                tvOpsi.setVisibility(View.GONE);
            }
            // Format harga dengan pemisah ribuan dan tampilkan
            tvHarga.setText("Rp" + String.format("%,d", item.getTotalHarga()).replace(',', '.'));
            // Tambahkan view item ke dalam container pesanan
            llPesananList.addView(itemView);
        }

        // Tampilkan total harga ke TextView
        tvTotalBayar.setText("Rp" + String.format("%,d", totalHarga).replace(',', '.'));

        // ── Klik tombol kembali ────────────────────
        // Tutup activity ini dan kembali ke DetailTenantActivity
        ivBack.setOnClickListener(v -> finish());

        // ── Klik tombol Bayar ───────────────────────
        btnBayar.setOnClickListener(v -> {
            // Tampilkan dialog konfirmasi sebelum benar-benar membayar
            new AlertDialog.Builder(PaymentActivity.this)
                    .setTitle("Konfirmasi Pembayaran")
                    .setMessage("Rp" + String.format("%,d", totalHarga).replace(',', '.') +
                            "\n\nScan QR di atas dan klik Bayar untuk simulasi berhasil.")
                    .setPositiveButton("Bayar", (dialog, which) -> {
                        // Simpan data pesanan ke Firebase dan buat notifikasi untuk tenant
                        simpanPesananDanNotifikasi();
                        Toast.makeText(PaymentActivity.this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show();
                        // Hapus isi holder agar tidak terbawa lagi
                        PesananHolder.clear();
                        // Kembali ke HomeActivity dan hapus activity sebelumnya
                        Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Batal", null) // Jika batal, dialog hilang
                    .show();
        });
    }

    /**
     * Menyimpan data pesanan ke Firebase dan membuat notifikasi untuk tenant.
     * Method ini dipanggil setelah user mengkonfirmasi pembayaran.
     */
    private void simpanPesananDanNotifikasi() {
        // Pastikan data lengkap
        if (pesananList == null || pesananList.isEmpty() || tenantId == null) return;

        // Generate ID pesanan unik berdasarkan waktu
        String pesananId = "P" + System.currentTimeMillis();
        // Ambil ID user yang sedang login dari SharedPreferences
        String customerId = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE)
                .getString("userId", "");
        // Nomor meja (sementara diisi manual, bisa dikembangkan)
        String meja = "A-01";

        // Buat objek PesananAdminModel untuk disimpan ke Firebase
        PesananAdminModel pesanan = new PesananAdminModel();
        pesanan.setId(pesananId);
        pesanan.setTenantId(tenantId);
        pesanan.setCustomerId(customerId);
        pesanan.setMeja(meja);
        // Waktu saat ini dalam format jam:menit
        pesanan.setWaktu(java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(new java.util.Date()));
        pesanan.setStatus("pending"); // Status awal pesanan

        // Konversi setiap PesananItem menjadi ItemPesananModel untuk disimpan
        List<ItemPesananModel> items = new ArrayList<>();
        for (PesananItem item : pesananList) {
            ItemPesananModel i = new ItemPesananModel();
            i.setMenuId(item.getMenuId());
            i.setNama(item.getNama());
            i.setQty(1);                       // Jumlah masih default 1
            i.setHarga(item.getHarga());
            i.setOpsi(item.getOpsi());
            i.setHargaTambahan(item.getHargaTambahan());
            items.add(i);
        }
        pesanan.setItems(items);
        pesanan.setTotalHarga(totalHarga);

        // ── Simpan ke Firebase Realtime Database node "pesanan" ──
        FirebaseDatabase.getInstance().getReference("pesanan")
                .child(pesananId).setValue(pesanan);

        // ── Buat notifikasi untuk tenant ──────────────────────
        String notifId = tenantId + "_" + System.currentTimeMillis();
        NotificationModel notif = new NotificationModel();
        notif.setId(notifId);
        notif.setTenantId(tenantId);
        // Teks notifikasi
        notif.setText("Pesanan baru " + pesananId + " dari Meja " + meja);
        notif.setWaktu(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()));
        notif.setStatus("unread"); // Belum dibaca

        // Simpan notifikasi ke node "notifications"
        FirebaseDatabase.getInstance().getReference("notifications")
                .child(notifId).setValue(notif);
    }
}