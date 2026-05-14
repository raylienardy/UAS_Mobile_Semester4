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
import com.example.foodcourtgo.admin.LoadingOut.LoadingOutActivity;
import com.example.foodcourtgo.admin.MejaManagement.MejaManagementActivity;
import com.example.foodcourtgo.model.TenantModel;
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
        setContentView(R.layout.admin_activity_tenant_management);

        etSearch = findViewById(R.id.et_search_tenant);
        rvTenant = findViewById(R.id.rv_tenant_list);
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        rvTenant.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapter dengan listener
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
            public void onAssignAkun(TenantModel tenant) {
                Intent intent = new Intent(TenantManagementActivity.this, AssignAkunActivity.class);
                intent.putExtra("tenantId", tenant.getId());
                startActivity(intent);
            }

            @Override
            public void onEditLokasi(TenantModel tenant) {
                showEditLokasiDialog(tenant);
            }

            @Override
            public void onMoveOwner(TenantModel tenant) {
                showMoveOwnerDialog(tenant); // pastikan method ini sudah ada di activity
            }
        });
        rvTenant.setAdapter(adapter);

        // Load data dari Firebase
        loadTenantData();

        // Fitur pencarian
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Tombol tambah tenant
        findViewById(R.id.btn_add_tenant).setOnClickListener(v ->
                startActivity(new Intent(this, AdminAddTenantActivity.class)));

        // Tombol back (jika ada di header)
//        View btnBack = findViewById(R.id.btn_back_tenant);
//        if (btnBack != null) {
//            btnBack.setOnClickListener(v -> finish());
//        }

        // Bottom navigation handling (jika ada)
        // Sesuaikan dengan ID di layout Anda
        findViewById(R.id.nav_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> {}); // sudah di halaman ini
        findViewById(R.id.btn_quick_meja).setOnClickListener(v ->
                startActivity(new Intent(this, MejaManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v ->
                startActivity(new Intent(this, com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v ->
                startActivity(new Intent(this, com.example.foodcourtgo.admin.Pesanan.PesananActivity.class)));
        findViewById(R.id.btn_quick_akun).setOnClickListener(v ->
                startActivity(new Intent(this, com.example.foodcourtgo.admin.AkunManagement.AkunManagementActivity.class)));
    }

    // Di dalam setup adapter, tambahkan:

    public void onMoveOwner(TenantModel tenant) {
        showMoveOwnerDialog(tenant);
    }

    // Method untuk memindahkan pemilik ke tenant lain
    private void showMoveOwnerDialog(TenantModel currentTenant) {
        // Ambil semua tenant dari tenantList (sudah ada)
        List<TenantModel> otherTenants = new ArrayList<>();
        for (TenantModel t : tenantList) {
            if (!t.getId().equals(currentTenant.getId())) {
                otherTenants.add(t);
            }
        }
        if (otherTenants.isEmpty()) {
            Toast.makeText(this, "Tidak ada tenant lain", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[otherTenants.size()];
        for (int i = 0; i < otherTenants.size(); i++) {
            names[i] = otherTenants.get(i).getNama() + " (" + otherTenants.get(i).getLokasi() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Pindah pemilik dari: " + currentTenant.getNama())
                .setItems(names, (dialog, which) -> {
                    TenantModel target = otherTenants.get(which);
                    swapOwner(currentTenant, target);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void swapOwner(TenantModel tenantA, TenantModel tenantB) {
        String ownerA = tenantA.getOwnerId();
        String ownerB = tenantB.getOwnerId();

        // Swap ownerId di node tenant
        tenantRef.child(tenantA.getId()).child("ownerId").setValue(ownerB);
        tenantRef.child(tenantB.getId()).child("ownerId").setValue(ownerA);

        // Update juga field tenantId di node akun
        DatabaseReference akunRef = FirebaseDatabase.getInstance().getReference("akun");
        if (ownerA != null && !ownerA.isEmpty()) {
            akunRef.child(ownerA).child("tenantId").setValue(tenantB.getId());
        }
        if (ownerB != null && !ownerB.isEmpty()) {
            akunRef.child(ownerB).child("tenantId").setValue(tenantA.getId());
        }

        Toast.makeText(this, "Pemilik ditukar", Toast.LENGTH_SHORT).show();
    }

    private void loadTenantData() {
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
                filter(etSearch.getText().toString());
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
                        tenant.getKategori().toLowerCase().contains(lower)) {
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
        input.setText(tenant.getLokasi());
        input.setHint("Lokasi (contoh: Area Barat, Lantai 2)");
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String lokasiBaru = input.getText().toString().trim();
            if (!lokasiBaru.isEmpty()) {
                DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
                tenantRef.child(tenant.getId()).child("lokasi").setValue(lokasiBaru)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(TenantManagementActivity.this, "Lokasi diperbarui", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
}