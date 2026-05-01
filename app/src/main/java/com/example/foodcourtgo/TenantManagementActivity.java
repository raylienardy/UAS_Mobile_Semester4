package com.example.foodcourtgo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TenantManagementActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvTenant;
    private TenantAdminAdapter adapter;
    private List<TenantModel> tenantList = new ArrayList<>();
    private List<TenantModel> filteredList = new ArrayList<>();
    private DatabaseReference tenantRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_management);
        // Ganti layout statis dengan RecyclerView di XML atau kita gunakan yang sudah ada?
        // Karena layout yang kamu buat masih card manual, kita perlu menyesuaikan.
        // Untuk menghemat waktu, kita akan modifikasi layout agar ada RecyclerView.
        // Sementara kita tambahkan RecyclerView di bawah search, dan hapus card manual.
        // Di sini saya asumsikan kamu sudah membuat container RecyclerView.
        // Jika belum, tambahkan RecyclerView di layout dengan id rv_tenant_list setelah btn_add_tenant.

        etSearch = findViewById(R.id.et_search_tenant);
        rvTenant = findViewById(R.id.rv_tenant_list); // perlu ditambahkan di layout
        // Jika belum ada, silakan tambahkan RecyclerView di activity_tenant_management.xml setelah btn_add_tenant.
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        rvTenant.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TenantAdminAdapter(filteredList, tenant -> {
            // Toggle status
            String newStatus = tenant.getStatus().equals("active") ? "inactive" : "active";
            tenantRef.child(tenant.getId()).child("status").setValue(newStatus);
        });
        rvTenant.setAdapter(adapter);

        loadTenants();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_add_tenant).setOnClickListener(v ->
                startActivity(new Intent(TenantManagementActivity.this, AdminAddTenantActivity.class)));

        // Bottom nav
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> {});
        findViewById(R.id.nav_menu).setOnClickListener(v -> startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> startActivity(new Intent(this, PesananActivity.class)));
        findViewById(R.id.nav_profil).setOnClickListener(v -> startActivity(new Intent(this, ProfilAdminActivity.class)));
    }

    private void loadTenants() {
        tenantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    TenantModel tenant = snap.getValue(TenantModel.class);
                    if (tenant != null) {
                        tenant.setId(snap.getKey());
                        tenantList.add(tenant);
                    }
                }
                filteredList.clear();
                filteredList.addAll(tenantList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filter(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) {
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