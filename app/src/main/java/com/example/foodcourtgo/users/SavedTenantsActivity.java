package com.example.foodcourtgo.users;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.TenantAdapter;
import com.example.foodcourtgo.model.TenantModel;
import com.example.foodcourtgo.users.menu.DetailTenantActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedTenantsActivity extends AppCompatActivity {

    private RecyclerView rvSavedTenants;
    private LinearLayout llEmpty;
    private ProgressBar progressBar;
    private TenantAdapter adapter;
    private List<TenantModel> savedTenantList = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_saved_tenants);

        rvSavedTenants = findViewById(R.id.rvSavedTenants);
        llEmpty = findViewById(R.id.llEmpty);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvSavedTenants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TenantAdapter(this, savedTenantList,
                tenant -> {
                    Intent intent = new Intent(SavedTenantsActivity.this, DetailTenantActivity.class);
                    intent.putExtra("tenantId", tenant.getId());
                    intent.putExtra("tenantNama", tenant.getNama());
                    intent.putExtra("tenantGambar", tenant.getGambar());
                    intent.putExtra("tenantKategori", tenant.getKategori());
                    intent.putExtra("tenantDeskripsi", tenant.getDeskripsi());
                    startActivity(intent);
                }, userId);
        rvSavedTenants.setAdapter(adapter);

        loadSavedTenants();
    }

    private void loadSavedTenants() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId);
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    llEmpty.setVisibility(View.VISIBLE);
                    return;
                }
                List<String> tenantIds = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    tenantIds.add(child.getKey());
                }
                if (tenantIds.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    llEmpty.setVisibility(View.VISIBLE);
                    return;
                }
                // Ambil data tenant berdasarkan ID
                DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
                tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot tenantSnap) {
                        savedTenantList.clear();
                        for (String id : tenantIds) {
                            DataSnapshot tenantSnapChild = tenantSnap.child(id);
                            TenantModel tenant = tenantSnapChild.getValue(TenantModel.class);
                            if (tenant != null) {
                                tenant.setId(id);
                                savedTenantList.add(tenant);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        if (savedTenantList.isEmpty()) {
                            llEmpty.setVisibility(View.VISIBLE);
                        } else {
                            llEmpty.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SavedTenantsActivity.this, "Gagal memuat tenant", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SavedTenantsActivity.this, "Gagal memuat favorit", Toast.LENGTH_SHORT).show();
            }
        });
    }
}