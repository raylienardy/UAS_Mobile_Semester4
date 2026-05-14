package com.example.foodcourtgo.users.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.CartHolder;
import com.example.foodcourtgo.model.CartItem;
import com.example.foodcourtgo.model.ItemPesananModel;
import com.example.foodcourtgo.model.NotificationModel;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.example.foodcourtgo.users.HomeActivity;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout llPesananList;
    private TextView tvTotalBayar;
    private Button btnBayar;

    private List<CartItem> cartList;
    private long totalHarga;
    private String tenantId;
    private String tenantNama;
    private String mejaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_menu_activity_payment);

        ivBack = findViewById(R.id.ivBack);
        llPesananList = findViewById(R.id.llPesananList);
        tvTotalBayar = findViewById(R.id.tvTotalBayar);
        btnBayar = findViewById(R.id.btnBayar);

        tenantId = getIntent().getStringExtra("tenantId");
        tenantNama = getIntent().getStringExtra("tenantNama");
        mejaId = getIntent().getStringExtra("mejaId");

        // Ambil data dari CartHolder
        cartList = CartHolder.getCartList();
        if (cartList == null || cartList.isEmpty()) {
            Toast.makeText(this, "Tidak ada pesanan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hitung total dan tampilkan item
        totalHarga = 0;
        for (CartItem item : cartList) {
            totalHarga += item.getTotalHarga();

            View itemView = getLayoutInflater().inflate(R.layout.item_pesanan, llPesananList, false);
            TextView tvNama = itemView.findViewById(R.id.tvPesananNama);
            TextView tvOpsi = itemView.findViewById(R.id.tvPesananOpsi);
            TextView tvHarga = itemView.findViewById(R.id.tvPesananHarga);

            String namaItem = item.getNama() + " x" + item.getQty();
            tvNama.setText(namaItem);
            if (item.getOpsi() != null && !item.getOpsi().isEmpty()) {
                tvOpsi.setText(item.getOpsi());
                tvOpsi.setVisibility(View.VISIBLE);
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
                        String createdId = simpanPesananDanNotifikasi();
                        if (createdId != null) {
                            Toast.makeText(PaymentActivity.this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show();
                            CartHolder.clear();
                            Intent intent = new Intent(PaymentActivity.this, StatusPesananActivity.class);
                            intent.putExtra("pesananId", createdId);
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
        if (cartList == null || cartList.isEmpty() || tenantId == null) return null;

        String pesananId = "P" + System.currentTimeMillis();
        SharedPreferences prefs = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        String customerId = prefs.getString("userId", "");
        String meja = (mejaId != null && !mejaId.isEmpty()) ? mejaId : "Take Away";

        // Format waktu lengkap: 15 Mei 2026, 14:30
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String waktuFormatted = sdf.format(new Date());

        PesananAdminModel pesanan = new PesananAdminModel();
        pesanan.setId(pesananId);
        pesanan.setTenantId(tenantId);
        pesanan.setTenantNama(tenantNama);
        pesanan.setCustomerId(customerId);
        pesanan.setMeja(meja);
        pesanan.setWaktu(waktuFormatted);  // format baru
        pesanan.setStatus("pending");

        List<ItemPesananModel> items = new ArrayList<>();
        for (CartItem item : cartList) {
            ItemPesananModel i = new ItemPesananModel();
            i.setMenuId(item.getMenuId());
            i.setNama(item.getNama());
            i.setQty(item.getQty());
            i.setHarga(item.getHarga() + item.getHargaTambahan()); // harga satuan
            i.setOpsi(item.getOpsi());
            i.setHargaTambahan(item.getHargaTambahan() * item.getQty());
            i.setCatatan(item.getCatatan());
            items.add(i);
        }
        pesanan.setItems(items);
        pesanan.setTotalHarga(totalHarga);

        FirebaseDatabase.getInstance().getReference("pesanan").child(pesananId).setValue(pesanan);

        // Notifikasi untuk tenant
        String notifId = tenantId + "_" + System.currentTimeMillis();
        NotificationModel notif = new NotificationModel();
        notif.setId(notifId);
        notif.setTenantId(tenantId);
        notif.setText("Pesanan baru " + pesananId + " dari " + meja);
        notif.setWaktu(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        notif.setStatus("unread");
        FirebaseDatabase.getInstance().getReference("notifications").child(notifId).setValue(notif);

        return pesananId;
    }
}