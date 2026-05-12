package com.example.foodcourtgo.tenant.pesanan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.addson.PesananAdminModel;
import com.example.foodcourtgo.tenant.dashboard.TenantDashboardActivity;
import com.example.foodcourtgo.tenant.menu.TenantMenuActivity;
import com.example.foodcourtgo.tenant.profil.TenantProfileActivity;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class TenantOrdersActivity extends AppCompatActivity {

    // ── View untuk daftar pesanan ───────────────────
    RecyclerView rvOrders;                     // Daftar pesanan tenant
    TenantOrderAdapter adapter;                // Adapter khusus tenant untuk menampilkan pesanan
    List<PesananAdminModel> allOrders = new ArrayList<>();   // Semua pesanan tenant ini dari Firebase

    // ── Data tenant dan referensi Firebase ──────────
    String tenantId;                           // ID tenant yang login
    DatabaseReference pesananRef;              // Referensi node "pesanan"

    // ── Tab filter ─────────────────────────────────
    TextView tabAll, tabPending, tabProcessing, tabDone;
    TextView tvEmpty;                          // Teks saat tidak ada pesanan
    String currentFilter = "all";              // Filter yang sedang aktif (all, pending, processing, done)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_orders); // Layout halaman pesanan tenant

        // ── Ambil tenantId dari SharedPreferences ──
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        // ── Inisialisasi view ──────────────────────
        rvOrders = findViewById(R.id.rv_tenant_orders);
        tabAll = findViewById(R.id.tab_orders_all);
        tabPending = findViewById(R.id.tab_orders_pending);
        tabProcessing = findViewById(R.id.tab_orders_process);
        tabDone = findViewById(R.id.tab_orders_done);
        tvEmpty = findViewById(R.id.tv_empty);

        // ── Setup RecyclerView ──────────────────────
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        // Adapter dengan listener klik: ketika item diklik → buka detail pesanan
        adapter = new TenantOrderAdapter(new ArrayList<>(), order -> {
            Intent i = new Intent(this, TenantOrderDetailActivity.class);
            i.putExtra("pesananId", order.getId());   // Kirim ID pesanan
            startActivity(i);
        });
        rvOrders.setAdapter(adapter);

        // ── Ambil data pesanan dari Firebase ────────
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        // Hanya pesanan milik tenant ini, urutkan berdasarkan tenantId
        pesananRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allOrders.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PesananAdminModel p = ds.getValue(PesananAdminModel.class);
                            if (p != null) {
                                p.setId(ds.getKey()); // Simpan key sebagai ID
                                allOrders.add(p);
                            }
                        }
                        applyFilter();  // Terapkan filter yang sedang aktif
                        // Tampilkan atau sembunyikan teks kosong
                        if (tvEmpty != null) {
                            if (allOrders.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                rvOrders.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                rvOrders.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });

        // ── Klik tab filter ─────────────────────────
        tabAll.setOnClickListener(v -> setFilter("all"));
        tabPending.setOnClickListener(v -> setFilter("pending"));
        tabProcessing.setOnClickListener(v -> setFilter("processing"));
        tabDone.setOnClickListener(v -> setFilter("done"));

        // ── Tombol kembali ──────────────────────────
        findViewById(R.id.btn_back_orders).setOnClickListener(v -> finish());

        // ══════════════════════════════════════════════
        // Bottom Navigation Tenant
        // ══════════════════════════════════════════════
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, TenantDashboardActivity.class)));
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v -> {}); // Halaman ini
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v ->
                startActivity(new Intent(this, TenantMenuActivity.class)));
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v ->
                startActivity(new Intent(this, TenantProfileActivity.class)));
    }

    /**
     * Mengganti filter dan memperbarui tampilan tab.
     */
    private void setFilter(String filter) {
        currentFilter = filter;
        // Ubah background tab sesuai filter yang aktif
        tabAll.setBackgroundResource(filter.equals("all") ? R.drawable.bg_nav_active : R.drawable.bg_card);
        tabPending.setBackgroundResource(filter.equals("pending") ? R.drawable.bg_nav_active : R.drawable.bg_card);
        tabProcessing.setBackgroundResource(filter.equals("processing") ? R.drawable.bg_nav_active : R.drawable.bg_card);
        tabDone.setBackgroundResource(filter.equals("done") ? R.drawable.bg_nav_active : R.drawable.bg_card);
        applyFilter();
    }

    /**
     * Menerapkan filter status pada daftar pesanan.
     * Jika filter "all" → tampilkan semua.
     * Lainnya → hanya pesanan dengan status yang sesuai.
     */
    private void applyFilter() {
        List<PesananAdminModel> filtered = new ArrayList<>();
        for (PesananAdminModel p : allOrders) {
            if (currentFilter.equals("all") || p.getStatus().equals(currentFilter)) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }
}