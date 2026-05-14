package com.example.foodcourtgo.admin.TenantManagement;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.AkunModel;
import com.example.foodcourtgo.model.TenantModel;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AssignAkunActivity extends AppCompatActivity {

    private String tenantId;
    private TenantModel tenant;
    private RecyclerView rvAkun;
    private List<AkunModel> akunList = new ArrayList<>();
    private DatabaseReference akunRef;
    private DatabaseReference tenantRef;
    private AkunAdapter adapter;
    private TextView tvCurrentOwner; // untuk menampilkan pemilik saat ini jika sudah ada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_assign_akun);

        tenantId = getIntent().getStringExtra("tenantId");
        tenant = (TenantModel) getIntent().getSerializableExtra("tenant");
        if (tenant == null) tenant = new TenantModel();

        akunRef = FirebaseDatabase.getInstance().getReference("akun");
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        rvAkun = findViewById(R.id.rv_akun_list);
        tvCurrentOwner = findViewById(R.id.tv_current_owner); // tambahkan di layout (opsional)
        rvAkun.setLayoutManager(new LinearLayoutManager(this));

        // Cek apakah tenant sudah punya ownerId
        if (tenant.getOwnerId() != null && !tenant.getOwnerId().isEmpty()) {
            // Tampilkan informasi pemilik saat ini dan tombol aksi
            showCurrentOwnerInfo();
        } else {
            // Belum ada owner, langsung tampilkan daftar akun yang bisa diassign
            loadAvailableAkun();
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void showCurrentOwnerInfo() {
        // Ambil data akun pemilik saat ini
        akunRef.child(tenant.getOwnerId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AkunModel owner = snapshot.getValue(AkunModel.class);
                if (owner != null) {
                    owner.setUserId(snapshot.getKey());
                    String info = "Pemilik saat ini: " + owner.getName() + " (" + owner.getUsername() + ")";
                    tvCurrentOwner.setVisibility(View.VISIBLE);
                    tvCurrentOwner.setText(info);
                }
                // Tampilkan tombol aksi: Ganti Assign, Hapus Assign
                showActionButtons();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showActionButtons() {
        LinearLayout buttonLayout = findViewById(R.id.button_layout); // tambahkan di layout
        buttonLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.btn_change_assign).setOnClickListener(v -> showChangeAssignDialog());
        findViewById(R.id.btn_remove_assign).setOnClickListener(v -> removeAssign());
    }

    private void removeAssign() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Assign")
                .setMessage("Yakin akan menghapus assign tenant ini? Pemilik akan kehilangan kaitan dengan stand.")
                .setPositiveButton("Hapus", (d, w) -> {
                    // Hapus relasi di tenant dan akun
                    tenantRef.child(tenantId).child("ownerId").removeValue();
                    akunRef.child(tenant.getOwnerId()).child("tenantId").removeValue();
                    // Role tidak diubah, tetap tenant (bisa diassign ke stand lain nanti)
                    Toast.makeText(this, "Assign dihapus", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showChangeAssignDialog() {
        // Tampilkan dialog daftar akun yang tersedia (customer + tenant belum punya stand)
        loadAvailableAkunForDialog();
    }

    private void loadAvailableAkunForDialog() {
        // Query akun dengan role customer atau tenant yang belum punya tenantId
        // Karena tidak bisa query OR, lakukan dua kali atau sekali ambil semua lalu filter
        akunRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AkunModel> available = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    AkunModel akun = snap.getValue(AkunModel.class);
                    if (akun != null) {
                        String role = akun.getRole();
                        String tenantIdAkun = akun.getTenantId();
                        // Tampilkan customer atau tenant yang belum punya stand
                        if (("customer".equals(role) || "tenant".equals(role)) && (tenantIdAkun == null || tenantIdAkun.isEmpty())) {
                            akun.setUserId(snap.getKey());
                            available.add(akun);
                        }
                    }
                }
                if (available.isEmpty()) {
                    Toast.makeText(AssignAkunActivity.this, "Tidak ada akun tersedia untuk diassign", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Tampilkan dalam AlertDialog dengan list
                String[] names = new String[available.size()];
                for (int i = 0; i < available.size(); i++) {
                    names[i] = available.get(i).getName() + " (" + available.get(i).getUsername() + ") - " + available.get(i).getRole();
                }
                new AlertDialog.Builder(AssignAkunActivity.this)
                        .setTitle("Pilih akun baru")
                        .setItems(names, (dialog, which) -> {
                            AkunModel selected = available.get(which);
                            reassignTo(selected);
                        })
                        .show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void reassignTo(AkunModel newOwner) {
        String oldOwnerId = tenant.getOwnerId();
        // Hapus relasi lama
        if (oldOwnerId != null && !oldOwnerId.isEmpty()) {
            tenantRef.child(tenantId).child("ownerId").removeValue();
            akunRef.child(oldOwnerId).child("tenantId").removeValue();
        }
        // Assign baru
        tenantRef.child(tenantId).child("ownerId").setValue(newOwner.getUserId());
        akunRef.child(newOwner.getUserId()).child("tenantId").setValue(tenantId);
        // Jika role masih customer, promosikan menjadi tenant
        if ("customer".equals(newOwner.getRole())) {
            akunRef.child(newOwner.getUserId()).child("role").setValue("tenant");
        }
        Toast.makeText(this, "Assign diganti ke " + newOwner.getName(), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadAvailableAkun() {
        // Tampilkan daftar akun yang bisa diassign (customer + tenant tanpa stand)
        akunRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                akunList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    AkunModel akun = snap.getValue(AkunModel.class);
                    if (akun != null) {
                        String role = akun.getRole();
                        String tenantIdAkun = akun.getTenantId();
                        if (("customer".equals(role) || "tenant".equals(role)) && (tenantIdAkun == null || tenantIdAkun.isEmpty())) {
                            akun.setUserId(snap.getKey());
                            akunList.add(akun);
                        }
                    }
                }
                adapter = new AkunAdapter(akunList, akun -> {
                    // Assign
                    tenantRef.child(tenantId).child("ownerId").setValue(akun.getUserId());
                    akunRef.child(akun.getUserId()).child("tenantId").setValue(tenantId);
                    if ("customer".equals(akun.getRole())) {
                        akunRef.child(akun.getUserId()).child("role").setValue("tenant");
                    }
                    Toast.makeText(AssignAkunActivity.this, "Akun " + akun.getUsername() + " ditugaskan ke " + tenant.getNama(), Toast.LENGTH_SHORT).show();
                    finish();
                });
                rvAkun.setAdapter(adapter);
                if (akunList.isEmpty()) {
                    Toast.makeText(AssignAkunActivity.this, "Tidak ada akun yang tersedia", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    static class AkunAdapter extends RecyclerView.Adapter<AkunAdapter.ViewHolder> {
        List<AkunModel> list;
        OnAkunClickListener listener;
        interface OnAkunClickListener { void onClick(AkunModel akun); }
        AkunAdapter(List<AkunModel> list, OnAkunClickListener listener) { this.list = list; this.listener = listener; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_akun_assign, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AkunModel akun = list.get(position);
            holder.tvName.setText(akun.getName());
            holder.tvUsername.setText(akun.getUsername());
            holder.tvRole.setText(akun.getRole()); // perlu tambahkan TextView di item layout
            holder.itemView.setOnClickListener(v -> listener.onClick(akun));
        }
        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvUsername, tvRole;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_akun_name);
                tvUsername = itemView.findViewById(R.id.tv_akun_username);
                tvRole = itemView.findViewById(R.id.tv_akun_role); // pastikan ID ada
            }
        }
    }
}