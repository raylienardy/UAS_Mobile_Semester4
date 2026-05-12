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
import com.example.foodcourtgo.model.PesananHolder;
import com.example.foodcourtgo.model.PesananItem;
import com.example.foodcourtgo.model.ItemPesananModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout llPesananList;
    private TextView tvTotalBayar;
    private Button btnBayar;

    private List<PesananItem> pesananList;
    private long totalHarga;
    private String tenantId;
    private String mejaId;        // tambahkan variabel mejaId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_menu_activity_payment);

        ivBack = findViewById(R.id.ivBack);
        llPesananList = findViewById(R.id.llPesananList);
        tvTotalBayar = findViewById(R.id.tvTotalBayar);
        btnBayar = findViewById(R.id.btnBayar);

        // Ambil data dari Intent
        tenantId = getIntent().getStringExtra("tenantId");
        mejaId = getIntent().getStringExtra("mejaId");
        if (mejaId == null || mejaId.isEmpty()) {
            mejaId = "Take Away";
        }

        pesananList = PesananHolder.getPesananList();
        if (pesananList == null || pesananList.isEmpty()) {
            Toast.makeText(this, "Tidak ada pesanan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        totalHarga = 0;
        for (PesananItem item : pesananList) {
            totalHarga += item.getTotalHarga();

            View itemView = getLayoutInflater().inflate(R.layout.item_pesanan, llPesananList, false);
            TextView tvNama = itemView.findViewById(R.id.tvPesananNama);
            TextView tvOpsi = itemView.findViewById(R.id.tvPesananOpsi);
            TextView tvHarga = itemView.findViewById(R.id.tvPesananHarga);

            tvNama.setText(item.getNama());
            if (item.getOpsi() != null && !item.getOpsi().isEmpty()) {
                tvOpsi.setText(item.getOpsi());
            } else {
                tvOpsi.setVisibility(View.GONE);
            }
            tvHarga.setText("Rp" + String.format("%,d", item.getTotalHarga()).replace(',', '.'));
            llPesananList.addView(itemView);
        }

        tvTotalBayar.setText("Rp" + String.format("%,d", totalHarga).replace(',', '.'));

        ivBack.setOnClickListener(v -> finish());

        btnBayar.setOnClickListener(v -> {
            new AlertDialog.Builder(PaymentActivity.this)
                    .setTitle("Konfirmasi Pembayaran")
                    .setMessage("Rp" + String.format("%,d", totalHarga).replace(',', '.') +
                            "\n\nScan QR di atas dan klik Bayar untuk simulasi berhasil.")
                    .setPositiveButton("Bayar", (dialog, which) -> {
                        String createdPesananId = simpanPesananDanNotifikasi();
                        if (createdPesananId != null) {
                            Toast.makeText(PaymentActivity.this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show();
                            PesananHolder.clear();
                            Intent intent = new Intent(PaymentActivity.this, StatusPesananActivity.class);
                            intent.putExtra("pesananId", createdPesananId);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(PaymentActivity.this, "Gagal menyimpan pesanan", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private String simpanPesananDanNotifikasi() {
        if (pesananList == null || pesananList.isEmpty() || tenantId == null) return null;

        String pesananId = "P" + System.currentTimeMillis();
        String customerId = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE)
                .getString("userId", "");

        // Buat objek PesananAdminModel
        PesananAdminModel pesanan = new PesananAdminModel();
        pesanan.setId(pesananId);
        pesanan.setTenantId(tenantId);
        pesanan.setCustomerId(customerId);
        pesanan.setMeja(mejaId);   // gunakan mejaId dari Intent
        pesanan.setWaktu(java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(new java.util.Date()));
        pesanan.setStatus("pending");

        List<ItemPesananModel> items = new ArrayList<>();
        for (PesananItem item : pesananList) {
            ItemPesananModel i = new ItemPesananModel();
            i.setMenuId(item.getMenuId());
            i.setNama(item.getNama());
            i.setQty(1);
            i.setHarga(item.getHarga());
            i.setOpsi(item.getOpsi());
            i.setHargaTambahan(item.getHargaTambahan());
            items.add(i);
        }
        pesanan.setItems(items);
        pesanan.setTotalHarga(totalHarga);

        // Simpan ke Firebase (hanya sekali)
        FirebaseDatabase.getInstance().getReference("pesanan")
                .child(pesananId).setValue(pesanan);

        // Buat notifikasi untuk tenant
        String notifId = tenantId + "_" + System.currentTimeMillis();
        NotificationModel notif = new NotificationModel();
        notif.setId(notifId);
        notif.setTenantId(tenantId);
        notif.setText("Pesanan baru " + pesananId + " dari Meja " + mejaId);
        notif.setWaktu(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()));
        notif.setStatus("unread");

        FirebaseDatabase.getInstance().getReference("notifications")
                .child(notifId).setValue(notif);

        return pesananId;
    }
}