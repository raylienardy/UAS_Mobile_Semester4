package com.example.foodcourtgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.users.menu.addson_PaymentActivity.PesananAdminModel;
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
        setContentView(R.layout.activity_tenant_dashboard);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");
        tenantName = pref.getString("namaUser", "Tenant");

        tvWelcome = findViewById(R.id.tv_tenant_welcome);
        tvTodayOrders = findViewById(R.id.tv_today_orders_value);
        tvProcessOrders = findViewById(R.id.tv_process_orders_value);
        tvDoneOrders = findViewById(R.id.tv_done_orders_value);
        tvTotalSales = findViewById(R.id.tv_total_sales_value);
        rvRecentOrders = findViewById(R.id.rv_recent_orders);

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
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v -> {});
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v ->
                startActivity(new Intent(this, TenantMenuActivity.class)));
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v ->
                startActivity(new Intent(this, TenantProfileActivity.class)));

        findViewById(R.id.btn_tenant_notification).setOnClickListener(v ->
                startActivity(new Intent(this, TenantNotificationsActivity.class)));
        findViewById(R.id.btn_view_all_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
    }
}