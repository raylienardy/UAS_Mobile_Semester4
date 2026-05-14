package com.example.foodcourtgo.admin.TenantManagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.TenantAdminAdapter;
import com.example.foodcourtgo.admin.AkunManagement.AkunManagementActivity;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.admin.MejaManagement.MejaManagementActivity;
import com.example.foodcourtgo.model.TenantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantManagementActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvTenant;
    private TenantAdminAdapter adapter;
    private List<TenantModel> tenantList = new ArrayList<>();
    private List<TenantModel> filteredList = new ArrayList<>();
    private DatabaseReference tenantRef;
    private DatabaseReference akunRef;
    private DatabaseReference menuRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_tenant_management);

        etSearch = findViewById(R.id.et_search_tenant);
        rvTenant = findViewById(R.id.rv_tenant_list);
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
        akunRef = FirebaseDatabase.getInstance().getReference("akun");
        menuRef = FirebaseDatabase.getInstance().getReference("menu");

        rvTenant.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TenantAdminAdapter(new TenantAdminAdapter.OnTenantActionListener() {
            @Override
            public void onToggleStatus(TenantModel tenant) {
                String newStatus = tenant.getStatus().equals("active") ? "inactive" : "active";
                tenantRef.child(tenant.getId()).child("status").setValue(newStatus)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(TenantManagementActivity.this,
                                        "Status tenant diubah", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onAssignResign(TenantModel tenant) {
                boolean hasOwner = tenant.getOwnerId() != null && !tenant.getOwnerId().isEmpty();
                if (hasOwner) {
                    // Resign: cabut assign
                    new AlertDialog.Builder(TenantManagementActivity.this)
                            .setTitle("Resign Tenant")
                            .setMessage("Yakin akan mencabut assign akun " + tenant.getOwnerName() + " dari tenant ini?")
                            .setPositiveButton("Resign", (d, w) -> {
                                String ownerId = tenant.getOwnerId();
                                tenantRef.child(tenant.getId()).child("ownerId").removeValue();
                                akunRef.child(ownerId).child("tenantId").removeValue();
                                akunRef.child(ownerId).child("role").setValue("customer");
                                Toast.makeText(TenantManagementActivity.this, "Assign dicabut", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                } else {
                    // Assign: buka AssignAkunActivity
                    Intent intent = new Intent(TenantManagementActivity.this, AssignAkunActivity.class);
                    intent.putExtra("tenantId", tenant.getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onEditLokasi(TenantModel tenant) {
                showEditLokasiDialog(tenant);
            }

            @Override
            public void onMoveOwner(TenantModel tenant) {
                showMoveOwnerDialog(tenant);
            }

            @Override
            public void onDeleteTenant(TenantModel tenant) {
                new AlertDialog.Builder(TenantManagementActivity.this)
                        .setTitle("Hapus Tenant")
                        .setMessage("Yakin akan menghapus tenant \"" + tenant.getNama() + "\"? Semua menu terkait juga akan dihapus.")
                        .setPositiveButton("Hapus", (d, w) -> {
                            // Hapus semua menu yang memiliki tenantId ini
                            menuRef.orderByChild("tenantId").equalTo(tenant.getId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot snap : snapshot.getChildren()) {
                                                snap.getRef().removeValue();
                                            }
                                            // Hapus tenant
                                            tenantRef.child(tenant.getId()).removeValue()
                                                    .addOnSuccessListener(unused ->
                                                            Toast.makeText(TenantManagementActivity.this, "Tenant dihapus", Toast.LENGTH_SHORT).show());
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });
        rvTenant.setAdapter(adapter);

        loadTenantData();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_add_tenant).setOnClickListener(v ->
                startActivity(new Intent(this, AdminAddTenantActivity.class)));

        // Bottom navigation
        findViewById(R.id.nav_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> {});
        findViewById(R.id.btn_quick_meja).setOnClickListener(v ->
                startActivity(new Intent(this, MejaManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v ->
                startActivity(new Intent(this, com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v ->
                startActivity(new Intent(this, com.example.foodcourtgo.admin.Pesanan.PesananActivity.class)));
        findViewById(R.id.btn_quick_akun).setOnClickListener(v ->
                startActivity(new Intent(this, AkunManagementActivity.class)));
    }

    private void loadTenantData() {
        tenantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();
                // Pertama, ambil semua data akun untuk mapping nama
                akunRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot akunSnapshot) {
                        Map<String, String> ownerNameMap = new HashMap<>();
                        for (DataSnapshot snap : akunSnapshot.getChildren()) {
                            String userId = snap.getKey();
                            String name = snap.child("name").getValue(String.class);
                            if (name != null) ownerNameMap.put(userId, name);
                        }

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            TenantModel tenant = snap.getValue(TenantModel.class);
                            if (tenant != null) {
                                tenant.setId(snap.getKey());
                                String ownerId = tenant.getOwnerId();
                                if (ownerId != null && !ownerId.isEmpty()) {
                                    tenant.setOwnerName(ownerNameMap.getOrDefault(ownerId, ""));
                                }
                                tenantList.add(tenant);
                            }
                        }
                        filter(etSearch.getText().toString());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TenantManagementActivity.this, "Gagal memuat data tenant", Toast.LENGTH_SHORT).show();
            }
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
                        (tenant.getKategori() != null && tenant.getKategori().toLowerCase().contains(lower))) {
                    filteredList.add(tenant);
                }
            }
        }
        adapter.setTenantList(filteredList);
    }

    private void showEditLokasiDialog(TenantModel tenant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Lokasi Tenant: " + tenant.getNama());
        final EditText input = new EditText(this);
        input.setText(tenant.getLokasi() != null ? tenant.getLokasi() : "");
        input.setHint("Lokasi (contoh: Area Barat, Lantai 2)");
        builder.setView(input);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String lokasiBaru = input.getText().toString().trim();
            if (!lokasiBaru.isEmpty()) {
                tenantRef.child(tenant.getId()).child("lokasi").setValue(lokasiBaru)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(TenantManagementActivity.this, "Lokasi diperbarui", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showMoveOwnerDialog(TenantModel currentTenant) {
        // Filter tenant lain yang memiliki ownerId (sudah ada pemilik)
        List<TenantModel> otherTenantsWithOwner = new ArrayList<>();
        for (TenantModel t : tenantList) {
            if (!t.getId().equals(currentTenant.getId())
                    && t.getOwnerId() != null && !t.getOwnerId().isEmpty()) {
                otherTenantsWithOwner.add(t);
            }
        }
        if (otherTenantsWithOwner.isEmpty()) {
            Toast.makeText(this, "Tidak ada tenant lain yang memiliki pemilik", Toast.LENGTH_SHORT).show();
            return;
        }

        // Siapkan array nama untuk ditampilkan: "Nama Tenant (Pemilik: xxx)"
        String[] names = new String[otherTenantsWithOwner.size()];
        for (int i = 0; i < otherTenantsWithOwner.size(); i++) {
            TenantModel t = otherTenantsWithOwner.get(i);
            String ownerName = t.getOwnerName() != null ? t.getOwnerName() : "Unknown";
            names[i] = t.getNama() + " (Pemilik: " + ownerName + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Pilih tenant tujuan untuk bertukar pemilik")
                .setItems(names, (dialog, which) -> {
                    TenantModel targetTenant = otherTenantsWithOwner.get(which);
                    swapOwner(currentTenant, targetTenant);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void swapOwner(TenantModel tenantA, TenantModel tenantB) {
        String ownerA = tenantA.getOwnerId();
        String ownerB = tenantB.getOwnerId();

        if (ownerA == null && ownerB == null) {
            Toast.makeText(this, "Kedua tenant tidak memiliki pemilik", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hapus relasi lama jika ada
        if (ownerA != null) {
            tenantRef.child(tenantA.getId()).child("ownerId").removeValue();
            akunRef.child(ownerA).child("tenantId").removeValue();
        }
        if (ownerB != null) {
            tenantRef.child(tenantB.getId()).child("ownerId").removeValue();
            akunRef.child(ownerB).child("tenantId").removeValue();
        }

        // Assign silang
        if (ownerA != null) {
            tenantRef.child(tenantB.getId()).child("ownerId").setValue(ownerA);
            akunRef.child(ownerA).child("tenantId").setValue(tenantB.getId());
            akunRef.child(ownerA).child("role").setValue("tenant");
        }
        if (ownerB != null) {
            tenantRef.child(tenantA.getId()).child("ownerId").setValue(ownerB);
            akunRef.child(ownerB).child("tenantId").setValue(tenantA.getId());
            akunRef.child(ownerB).child("role").setValue("tenant");
        }

        Toast.makeText(this, "Pemilik ditukar", Toast.LENGTH_SHORT).show();
    }
}