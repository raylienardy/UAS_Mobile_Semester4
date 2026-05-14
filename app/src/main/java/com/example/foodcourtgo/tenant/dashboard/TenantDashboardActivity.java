package com.example.foodcourtgo.tenant.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.RecentOrderAdapter;
import com.example.foodcourtgo.tenant.laporan.TenantLaporanActivity;
import com.example.foodcourtgo.tenant.menu.TenantMenuActivity;
import com.example.foodcourtgo.tenant.pesanan.TenantOrdersActivity;
import com.example.foodcourtgo.tenant.profil.TenantProfileActivity;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TenantDashboardActivity extends AppCompatActivity {

    TextView tvWelcome, tvTodayOrders, tvProcessOrders, tvDoneOrders, tvTotalSales;
    RecyclerView rvRecentOrders;
    RecentOrderAdapter recentAdapter;
    List<PesananAdminModel> recentOrderList = new ArrayList<>();

    // Tombol notifikasi
    TextView btnNotification;

    String tenantId, tenantName;
    DatabaseReference pesananRef, notifRef;
    ValueEventListener notifListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_dashboard);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");
        tenantName = pref.getString("namaUser", "Tenant");

        tvWelcome = findViewById(R.id.tv_tenant_welcome);
        tvTodayOrders = findViewById(R.id.tv_today_orders_value);
        tvProcessOrders = findViewById(R.id.tv_process_orders_value);
        tvDoneOrders = findViewById(R.id.tv_done_orders_value);
        tvTotalSales = findViewById(R.id.tv_total_sales_value);
        rvRecentOrders = findViewById(R.id.rv_recent_orders);
        btnNotification = findViewById(R.id.btn_tenant_notification);

        tvWelcome.setText("Halo, " + tenantName);

        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new RecentOrderAdapter(recentOrderList);
        rvRecentOrders.setAdapter(recentAdapter);

        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        notifRef = FirebaseDatabase.getInstance().getReference("notifications");

        // ======================== BADGE NOTIFIKASI ========================
        // Hitung notifikasi yang belum dibaca (status != "read")
        notifListener = notifRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int unreadCount = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String status = ds.child("status").getValue(String.class);
                            if (status == null || !status.equals("read")) {
                                unreadCount++;
                            }
                        }
                        // Update badge pada tombol notifikasi
                        if (unreadCount > 0) {
                            btnNotification.setText(String.valueOf(unreadCount));
                            // Opsional: ubah background agar terlihat seperti badge
                            btnNotification.setBackgroundResource(R.drawable.bg_badge_notif);
                            btnNotification.setTextColor(getColor(R.color.white));
                        } else {
                            btnNotification.setText("!");
                            btnNotification.setBackgroundResource(R.drawable.bg_nav_active); // kembalikan ke style awal
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Jika gagal, jangan tampilkan badge
                        btnNotification.setText("!");
                    }
                });

        // ======================== STATISTIK PESANAN ========================
        pesananRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pending = 0, processing = 0, done = 0;
                        long totalSales = 0;
                        List<PesananAdminModel> allOrders = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PesananAdminModel p = ds.getValue(PesananAdminModel.class);
                            if (p == null) continue;
                            p.setId(ds.getKey());
                            allOrders.add(p);

                            String status = p.getStatus();
                            if ("pending".equals(status)) pending++;
                            else if ("processing".equals(status)) processing++;
                            else if ("done".equals(status)) {
                                done++;
                                totalSales += p.getTotalHarga();
                            }
                        }
                        tvTodayOrders.setText(String.valueOf(pending + processing + done));
                        tvProcessOrders.setText(String.valueOf(processing));
                        tvDoneOrders.setText(String.valueOf(done));
                        tvTotalSales.setText("Rp " + String.format(Locale.getDefault(), "%,d", totalSales));

                        // Urutkan berdasarkan ID (descending) untuk menampilkan pesanan terbaru
                        Collections.sort(allOrders, (o1, o2) -> o2.getId().compareTo(o1.getId()));
                        recentOrderList.clear();
                        int count = Math.min(allOrders.size(), 5);
                        for (int i = 0; i < count; i++) {
                            recentOrderList.add(allOrders.get(i));
                        }
                        recentAdapter.updateList(recentOrderList);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // ======================== BOTTOM NAVIGATION ========================
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v -> {});
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v ->
                startActivity(new Intent(this, TenantMenuActivity.class)));
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v ->
                startActivity(new Intent(this, TenantProfileActivity.class)));

        // Tombol notifikasi
        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, TenantNotificationsActivity.class)));

        // Tombol "Lihat Semua" pesanan terbaru
        findViewById(R.id.btn_view_all_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));

        findViewById(R.id.btn_laporan_bulanan).setOnClickListener(v ->
                startActivity(new Intent(this, TenantLaporanActivity.class)));

        findViewById(R.id.btn_tenant_emergency).setOnClickListener(v -> showEmergencyDialog());

    }

    private void showEmergencyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Kirim Darurat")
                .setMessage("Kirim notifikasi ke admin bahwa Anda membutuhkan bantuan?")
                .setPositiveButton("Kirim", (d, w) -> {
                    String emergencyId = "EMG_" + System.currentTimeMillis();
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", emergencyId);
                    data.put("tenantId", tenantId);
                    data.put("tenantName", tenantName);
                    data.put("message", "Tenant " + tenantName + " membutuhkan bantuan!");
                    data.put("timestamp", System.currentTimeMillis());
                    data.put("status", "unread");
                    FirebaseDatabase.getInstance().getReference("emergency").child(emergencyId)
                            .setValue(data)
                            .addOnSuccessListener(a -> Toast.makeText(this, "Darurat terkirim", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Gagal mengirim", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifListener != null) notifRef.removeEventListener(notifListener);
    }
}