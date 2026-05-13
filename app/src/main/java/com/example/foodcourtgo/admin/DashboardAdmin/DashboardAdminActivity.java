package com.example.foodcourtgo.admin.DashboardAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.RecentOrderAdminAdapter;
import com.example.foodcourtgo.admin.AkunManagement.AkunManagementActivity;
import com.example.foodcourtgo.admin.Emergency.EmergencyActivity;
import com.example.foodcourtgo.admin.LoadingOut.LoadingOutActivity;
import com.example.foodcourtgo.admin.MejaManagement.MejaManagementActivity;
import com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity;
import com.example.foodcourtgo.admin.Pesanan.DetailPesananActivity;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.ProfilAdmin.ProfilAdminActivity;
import com.example.foodcourtgo.admin.TenantManagement.TenantManagementActivity;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardAdminActivity extends AppCompatActivity {

    // --- Views ---
    private TextView tvTotalOrders;
    private TextView tvTotalRevenue;
    private TextView tvActiveTenant;
    private TextView tvMenuCount;
    private TextView tvTableCount;
    private RecyclerView rvRecentOrders;

    // --- Firebase References ---
    private DatabaseReference pesananRef;
    private DatabaseReference tenantRef;
    private DatabaseReference menuRef;
    private DatabaseReference mejaRef;

    // --- Adapter ---
    private RecentOrderAdminAdapter recentOrderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_dashboard_admin);

        initViews();
        initFirebaseRefs();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvActiveTenant = findViewById(R.id.tv_active_tenant);
        tvMenuCount = findViewById(R.id.tv_menu_count);
        tvTableCount = findViewById(R.id.tv_table_count);
        rvRecentOrders = findViewById(R.id.rv_recent_orders);
    }

    private void initFirebaseRefs() {
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
        menuRef = FirebaseDatabase.getInstance().getReference("menu");
        mejaRef = FirebaseDatabase.getInstance().getReference("meja");
    }

    private void setupRecyclerView() {
        if (rvRecentOrders != null) {
            rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
            recentOrderAdapter = new RecentOrderAdminAdapter(order -> {
                Intent intent = new Intent(DashboardAdminActivity.this, DetailPesananActivity.class);
                intent.putExtra("pesananId", order.getId());
                startActivity(intent);
            });
            rvRecentOrders.setAdapter(recentOrderAdapter);
        }
    }

    private void setupClickListeners() {
        // Bottom navigation
        findViewByIdSafe(R.id.nav_dashboard, v -> {});
        findViewByIdSafe(R.id.nav_tenant, v ->
                startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewByIdSafe(R.id.nav_menu, v ->
                startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewByIdSafe(R.id.nav_pesanan, v ->
                startActivity(new Intent(this, PesananActivity.class)));
        findViewByIdSafe(R.id.nav_profil, v ->
                startActivity(new Intent(this, ProfilAdminActivity.class)));

        // Aksi cepat (quick actions)
        findViewByIdSafe(R.id.btn_quick_tenant, v ->
                startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewByIdSafe(R.id.btn_quick_menu, v ->
                startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewByIdSafe(R.id.btn_quick_report, v ->
                startActivity(new Intent(this, LaporanActivity.class)));
        findViewByIdSafe(R.id.btn_profile_shortcut, v ->
                startActivity(new Intent(this, ProfilAdminActivity.class)));
        findViewByIdSafe(R.id.btn_quick_meja, v ->
                startActivity(new Intent(this, MejaManagementActivity.class)));
        findViewByIdSafe(R.id.btn_quick_akun, v ->
                startActivity(new Intent(this, AkunManagementActivity.class)));
        findViewById(R.id.btn_quick_emergency).setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyActivity.class)));
        findViewById(R.id.btn_quick_loadingout).setOnClickListener(v ->
                startActivity(new Intent(this, LoadingOutActivity.class)));
    }

    // Helper method yang aman untuk null view
    private void findViewByIdSafe(int id, View.OnClickListener listener) {
        View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(listener);
        } else {
            android.util.Log.e("DashboardAdmin", "View dengan ID " + id + " tidak ditemukan di layout");
        }
    }

    private void loadData() {
        loadDashboardData();
        loadTableCount();
        loadRecentOrders();
    }

    private void loadDashboardData() {
        // Total pesanan & pendapatan
        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalOrders = (int) snapshot.getChildrenCount();
                long totalRevenue = 0;
                for (DataSnapshot pesananSnap : snapshot.getChildren()) {
                    Object totalObj = pesananSnap.child("totalHarga").getValue();
                    if (totalObj instanceof Long) {
                        totalRevenue += (Long) totalObj;
                    } else if (totalObj instanceof Integer) {
                        totalRevenue += ((Integer) totalObj).longValue();
                    }
                }
                tvTotalOrders.setText(String.valueOf(totalOrders));
                tvTotalRevenue.setText("Rp " + String.format("%,d", totalRevenue).replace(',', '.'));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { /* handle error if needed */ }
        });

        // Tenant aktif
        tenantRef.orderByChild("status").equalTo("active")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvActiveTenant.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Jumlah menu
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvMenuCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTableCount() {
        mejaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTableCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadRecentOrders() {
        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PesananAdminModel> allOrders = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PesananAdminModel order = snap.getValue(PesananAdminModel.class);
                    if (order != null) {
                        order.setId(snap.getKey());
                        allOrders.add(order);
                    }
                }
                // Urutkan descending berdasarkan waktu
                Collections.sort(allOrders, (a, b) -> {
                    String wa = a.getWaktu();
                    String wb = b.getWaktu();
                    if (wa == null) return 1;
                    if (wb == null) return -1;
                    return wb.compareTo(wa);
                });
                // Ambil 5 terbaru
                List<PesananAdminModel> recent = allOrders.size() > 5 ? allOrders.subList(0, 5) : allOrders;
                if (recentOrderAdapter != null) {
                    recentOrderAdapter.setOrders(recent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}