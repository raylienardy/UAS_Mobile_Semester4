package com.example.foodcourtgo.admin.TenantManagement;

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
        rvAkun.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AkunAdapter(akunList, akun -> {
            // Assign akun ke tenant
            tenantRef.child(tenantId).child("ownerId").setValue(akun.getUserId());
            akunRef.child(akun.getUserId()).child("tenantId").setValue(tenantId);
            Toast.makeText(this, "Akun " + akun.getUsername() + " ditugaskan ke " + tenant.getNama(), Toast.LENGTH_SHORT).show();
            finish();
        });
        rvAkun.setAdapter(adapter);

        loadAkunBelumAssign();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadAkunBelumAssign() {
        akunRef.orderByChild("role").equalTo("tenant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                akunList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    AkunModel akun = snap.getValue(AkunModel.class);
                    if (akun != null && (akun.getTenantId() == null || akun.getTenantId().isEmpty())) {
                        akun.setUserId(snap.getKey());
                        akunList.add(akun);
                    }
                }
                adapter.notifyDataSetChanged();
                if (akunList.isEmpty()) Toast.makeText(AssignAkunActivity.this, "Tidak ada akun tenant yang tersedia", Toast.LENGTH_SHORT).show();
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
            holder.itemView.setOnClickListener(v -> listener.onClick(akun));
        }
        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvUsername;
            ViewHolder(@NonNull View itemView) { super(itemView); tvName = itemView.findViewById(R.id.tv_akun_name); tvUsername = itemView.findViewById(R.id.tv_akun_username); }
        }
    }
}