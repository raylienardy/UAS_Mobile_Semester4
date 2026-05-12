package com.example.foodcourtgo.tenant.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.tenant.menu.TenantMenuActivity;
import com.example.foodcourtgo.tenant.pesanan.TenantOrdersActivity;
import com.example.foodcourtgo.tenant.profil.TenantProfileActivity;
import com.example.foodcourtgo.addson.PesananAdminModel;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TenantDashboardActivity extends AppCompatActivity {

    TextView tvWelcome, tvTodayOrders, tvProcessOrders, tvDoneOrders, tvTotalSales;
    RecyclerView rvRecentOrders;
    RecentOrderAdapter recentAdapter;
    List<PesananAdminModel> recentOrderList = new ArrayList<>();

    String tenantId, tenantName;
    DatabaseReference pesananRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.foodcourtgo.R.layout.tenant_activity_tenant_dashboard);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");
        tenantName = pref.getString("namaUser", "Tenant");

        tvWelcome = findViewById(com.example.foodcourtgo.R.id.tv_tenant_welcome);
        tvTodayOrders = findViewById(com.example.foodcourtgo.R.id.tv_today_orders_value);
        tvProcessOrders = findViewById(com.example.foodcourtgo.R.id.tv_process_orders_value);
        tvDoneOrders = findViewById(com.example.foodcourtgo.R.id.tv_done_orders_value);
        tvTotalSales = findViewById(com.example.foodcourtgo.R.id.tv_total_sales_value);
        rvRecentOrders = findViewById(com.example.foodcourtgo.R.id.rv_recent_orders);

        tvWelcome.setText("Halo, " + tenantName);

        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new RecentOrderAdapter(recentOrderList);
        rvRecentOrders.setAdapter(recentAdapter);

        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");

        // Statistik utama
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

                        // Urutkan berdasarkan waktu terbaru (asumsi waktu bisa dibandingkan, jika tidak pakai key)
                        Collections.sort(allOrders, (o1, o2) -> o2.getId().compareTo(o1.getId()));
                        // Ambil maksimal 5
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

        // Bottom Navigation
        // Dashboard
        findViewById(com.example.foodcourtgo.R.id.nav_tenant_dashboard).setOnClickListener(v -> {});

        // Pesanan
        findViewById(com.example.foodcourtgo.R.id.nav_tenant_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));

        // Menu
        findViewById(com.example.foodcourtgo.R.id.nav_tenant_menu).setOnClickListener(v ->
                startActivity(new Intent(this, TenantMenuActivity.class)));

        // Profile
        findViewById(com.example.foodcourtgo.R.id.nav_tenant_profile).setOnClickListener(v ->
                startActivity(new Intent(this, TenantProfileActivity.class)));

        // ========================================================================================================

        // notifikasi
        findViewById(com.example.foodcourtgo.R.id.btn_tenant_notification).setOnClickListener(v ->
                startActivity(new Intent(this, TenantNotificationsActivity.class)));
        // notifikasi
        findViewById(R.id.btn_view_all_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
    }
}