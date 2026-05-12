package com.example.foodcourtgo.admin.TenantManagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.TenantModel;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.ProfilAdminActivity.ProfilAdminActivity;
import com.example.foodcourtgo.adapter.TenantAdminAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TenantManagementActivity extends AppCompatActivity {

    // ── View untuk pencarian dan daftar tenant ─────
    private EditText etSearch;                 // Kolom pencarian tenant
    private RecyclerView rvTenant;             // Daftar tenant dalam bentuk RecyclerView

    // ── Adapter dan list data ────────────────────
    private TenantAdminAdapter adapter;        // Adapter khusus admin untuk menampilkan tenant
    private List<TenantModel> tenantList = new ArrayList<>();       // Semua tenant dari Firebase
    private List<TenantModel> filteredList = new ArrayList<>();     // Hasil filter pencarian
    private DatabaseReference tenantRef;       // Referensi Firebase ke node "tenant"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan ke layout admin_activity_tenant_management.xml
        setContentView(R.layout.admin_activity_tenant_management);

        // ── Inisialisasi view ──────────────────────
        etSearch = findViewById(R.id.et_search_tenant);
        rvTenant = findViewById(R.id.rv_tenant_list);   // RecyclerView untuk daftar tenant

        // ── Referensi Firebase ──────────────────────
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        // ── Setup RecyclerView ──────────────────────
        rvTenant.setLayoutManager(new LinearLayoutManager(this));

        // Inisialisasi adapter admin.
        // Setiap kali tenant diklik (di dalam adapter), status tenant akan di-toggle (active <-> inactive)
        adapter = new TenantAdminAdapter(filteredList, tenant -> {
            // Toggle status: jika active -> inactive, sebaliknya
            String newStatus = tenant.getStatus().equals("active") ? "inactive" : "active";
            // Update status di Firebase pada child "status" tenant tersebut
            tenantRef.child(tenant.getId()).child("status").setValue(newStatus);
        });
        rvTenant.setAdapter(adapter);

        // ── Muat data tenant dari Firebase ──────────
        loadTenants();

        // ── Fitur pencarian tenant ─────────────────
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());   // Filter saat teks berubah
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ── Tombol "+ Tambah Tenant" ────────────────
        // Membuka halaman AdminAddTenantActivity untuk menambahkan tenant baru
        findViewById(R.id.btn_add_tenant).setOnClickListener(v ->
                startActivity(new Intent(TenantManagementActivity.this, AdminAddTenantActivity.class)));

        // ═══════════════════════════════════════════
        // Bottom Navigation (sama seperti Dashboard)
        // ═══════════════════════════════════════════
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> {}); // Sudah di halaman ini
        findViewById(R.id.nav_menu).setOnClickListener(v -> startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> startActivity(new Intent(this, PesananActivity.class)));
        findViewById(R.id.nav_profil).setOnClickListener(v -> startActivity(new Intent(this, ProfilAdminActivity.class)));
    }

    /**
     * Mengambil semua data tenant dari Firebase Realtime Database.
     * Data dipantau secara realtime (addValueEventListener).
     */
    private void loadTenants() {
        tenantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();  // Kosongkan list lama
                for (DataSnapshot snap : snapshot.getChildren()) {
                    TenantModel tenant = snap.getValue(TenantModel.class);
                    if (tenant != null) {
                        tenant.setId(snap.getKey());   // Simpan key Firebase sebagai ID
                        tenantList.add(tenant);
                    }
                }
                // Setelah data diambil, masukkan semua ke filteredList (tanpa filter)
                filteredList.clear();
                filteredList.addAll(tenantList);
                adapter.notifyDataSetChanged();   // Perbarui RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gagal mengambil data
            }
        });
    }

    /**
     * Memfilter daftar tenant berdasarkan kata kunci yang diketik di kolom pencarian.
     * Pencarian berdasarkan nama atau kategori tenant.
     */
    private void filter(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) {
            // Jika kosong, tampilkan semua
            filteredList.addAll(tenantList);
        } else {
            String lower = keyword.toLowerCase();
            for (TenantModel tenant : tenantList) {
                if (tenant.getNama().toLowerCase().contains(lower) ||
                        tenant.getKategori().toLowerCase().contains(lower)) {
                    filteredList.add(tenant);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}