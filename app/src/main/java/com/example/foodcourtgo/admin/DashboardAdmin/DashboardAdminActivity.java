package com.example.foodcourtgo.admin.DashboardAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.RecentOrderAdminAdapter;
import com.example.foodcourtgo.admin.MejaManagement.MejaManagementActivity;
//import com.example.foodcourtgo.admin.MejaManagementActivity;
import com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity;
import com.example.foodcourtgo.admin.Pesanan.DetailPesananActivity;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.ProfilAdminActivity.ProfilAdminActivity;
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

    // ── Empat kartu statistik di dashboard ──────────────
    private TextView tvTotalOrders;      // Menampilkan total pesanan
    private TextView tvTotalRevenue;     // Menampilkan total pendapatan
    private TextView tvActiveTenant;     // Menampilkan jumlah tenant aktif
    private TextView tvMenuCount;        // Menampilkan jumlah menu tersedia

    // ── Referensi ke node Firebase ──────────────────────
    private DatabaseReference pesananRef;  // Referensi ke node "pesanan"
    private DatabaseReference tenantRef;   // Referensi ke node "tenant"
    private DatabaseReference menuRef;     // Referensi ke node "menu"

    private TextView tvTableCount;
    private RecyclerView rvRecentOrders;
    private RecentOrderAdminAdapter recentOrderAdapter;
    private DatabaseReference mejaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hubungkan activity dengan layout XML dashboard admin
        setContentView(R.layout.admin_activity_dashboard_admin);



        // ── Inisialisasi TextView statistik ──────────────
        tvTotalOrders  = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvActiveTenant = findViewById(R.id.tv_active_tenant);
        tvMenuCount    = findViewById(R.id.tv_menu_count);

        // ── Inisialisasi referensi Firebase ──────────────
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        tenantRef  = FirebaseDatabase.getInstance().getReference("tenant");
        menuRef    = FirebaseDatabase.getInstance().getReference("menu");

        mejaRef = FirebaseDatabase.getInstance().getReference("meja");
        tvTableCount = findViewById(R.id.tv_table_count);

        // Setup RecyclerView pesanan terbaru
        rvRecentOrders = findViewById(R.id.rv_recent_orders);
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        recentOrderAdapter = new RecentOrderAdminAdapter(order -> {
            Intent intent = new Intent(DashboardAdminActivity.this, DetailPesananActivity.class);
            intent.putExtra("pesananId", order.getId());
            startActivity(intent);
        });
        rvRecentOrders.setAdapter(recentOrderAdapter);

        // ── Muat data statistik dari Firebase ───────────
        loadDashboardData();
        loadTableCount();
        loadRecentOrders();

        // ══════════════════════════════════════════════════
        // Bottom Navigation (navigasi bawah)
        // ══════════════════════════════════════════════════

        // Nav "Home" (dashboard ini sendiri) → tidak perlu pindah
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {});

        // Nav "Tenant" → ke halaman manajemen tenant
        findViewById(R.id.nav_tenant).setOnClickListener(v ->
                startActivity(new Intent(this, TenantManagementActivity.class)));

        // Nav "Menu" → ke halaman manajemen menu
        findViewById(R.id.nav_menu).setOnClickListener(v ->
                startActivity(new Intent(this, MenuManagementActivity.class)));

        // Nav "Order" → ke halaman daftar pesanan
        findViewById(R.id.nav_pesanan).setOnClickListener(v ->
                startActivity(new Intent(this, PesananActivity.class)));

        // Nav "Profil" → ke halaman profil admin
        findViewById(R.id.nav_profil).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilAdminActivity.class)));

        // ══════════════════════════════════════════════════
        // Aksi Cepat (Quick Actions)
        // ══════════════════════════════════════════════════

        // Tombol "+ Tenant" → tambah tenant baru
        findViewById(R.id.btn_quick_tenant).setOnClickListener(v ->
                startActivity(new Intent(this, TenantManagementActivity.class)));

        // Tombol "+ Menu" → kelola menu
        findViewById(R.id.btn_quick_menu).setOnClickListener(v ->
                startActivity(new Intent(this, MenuManagementActivity.class)));

        // Tombol "Laporan" → lihat laporan
        findViewById(R.id.btn_quick_report).setOnClickListener(v ->
                startActivity(new Intent(this, LaporanActivity.class)));

        // Tombol avatar (inisial "A" di header kanan atas) → ke profil
        findViewById(R.id.btn_profile_shortcut).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilAdminActivity.class)));

        // Tombol "Meja" → ke halaman manajemen meja
        findViewById(R.id.btn_quick_meja).setOnClickListener(v ->
                startActivity(new Intent(this, MejaManagementActivity.class)));
    }

    /**
     * Mengambil dan menampilkan data statistik dari Firebase Realtime Database.
     * Ada tiga listener terpisah: untuk pesanan, tenant, dan menu.
     */
    private void loadDashboardData() {
        // ── 1. Total pesanan & pendapatan ───────────────
        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Jumlah total pesanan = jumlah anak (child) di node "pesanan"
                int totalOrders = (int) snapshot.getChildrenCount();
                long totalRevenue = 0;

                // Loop setiap data pesanan untuk ambil totalHarga
                for (DataSnapshot pesananSnap : snapshot.getChildren()) {
                    Object totalObj = pesananSnap.child("totalHarga").getValue();
                    if (totalObj != null) {
                        // Konversi ke long (sesuai tipe data yang disimpan)
                        totalRevenue += (long) totalObj;
                    }
                }

                // Tampilkan ke TextView
                tvTotalOrders.setText(String.valueOf(totalOrders));
                // Format pendapatan dengan pemisah ribuan
                tvTotalRevenue.setText("Rp " + String.format("%,d", totalRevenue).replace(',', '.'));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Bisa ditambahkan Toast jika perlu
            }
        });

        // ── 2. Tenant aktif (status = "active") ─────────
        // Query child "status" yang bernilai "active" di node "tenant"
        tenantRef.orderByChild("status").equalTo("active")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int activeCount = (int) snapshot.getChildrenCount();
                        tvActiveTenant.setText(String.valueOf(activeCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // ── 3. Total menu tersedia ──────────────────────
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int menuCount = (int) snapshot.getChildrenCount();
                tvMenuCount.setText(String.valueOf(menuCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTableCount() {
        mejaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                tvTableCount.setText(String.valueOf(count));
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
                // Urutkan berdasarkan waktu terbaru (descending)
                Collections.sort(allOrders, (a, b) -> {
                    String waktuA = a.getWaktu();
                    String waktuB = b.getWaktu();
                    if (waktuA == null) return 1;
                    if (waktuB == null) return -1;
                    return waktuB.compareTo(waktuA);
                });
                // Ambil 5 terbaru
                List<PesananAdminModel> recent = allOrders.size() > 5 ? allOrders.subList(0, 5) : allOrders;
                recentOrderAdapter.setOrders(recent);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}