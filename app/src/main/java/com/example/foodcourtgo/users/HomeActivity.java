package com.example.foodcourtgo.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.login.MainActivity;

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

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvTenants;
    private LinearLayout llEmpty;
    private EditText etSearch;
    private TextView tvSidebarNama, tvSidebarUserId, tvSidebarAvatar;
    private TenantAdapter adapter;
    private List<TenantModel> tenantList = new ArrayList<>();
    private List<TenantModel> tenantListFiltered = new ArrayList<>();
    private String userId;
    private String namaUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_home);

        // === TAMBAHAN: Ambil data dari Intent dan simpan ke SharedPreferences ===
        Intent intent = getIntent();
        if (intent != null) {
            String orderMode = intent.getStringExtra("orderMode");
            String mejaId = intent.getStringExtra("mejaId");
            if (orderMode != null || mejaId != null) {
                SharedPreferences prefs = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                if (orderMode != null) {
                    editor.putString("orderMode", orderMode);
                }
                if (mejaId != null) {
                    editor.putString("mejaId", mejaId);
                }
                editor.apply();
            }
        }
        // === END TAMBAHAN ===

        drawerLayout = findViewById(R.id.drawerLayout);
        rvTenants = findViewById(R.id.rvTenants);
        llEmpty = findViewById(R.id.llEmpty);
        etSearch = findViewById(R.id.etSearch);
        tvSidebarNama = findViewById(R.id.tvSidebarNama);
        tvSidebarUserId = findViewById(R.id.tvSidebarUserId);
        tvSidebarAvatar = findViewById(R.id.tvSidebarAvatar);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        userId = pref.getString("userId", "");
        namaUser = pref.getString("namaUser", "User");

        if (userId.isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        tvSidebarNama.setText(namaUser);
        tvSidebarUserId.setText("ID: " + userId);
        tvSidebarAvatar.setText(namaUser.substring(0, 1).toUpperCase());

        adapter = new TenantAdapter(this, tenantListFiltered,
                tenant -> {
                    Intent detailIntent = new Intent(HomeActivity.this, DetailTenantActivity.class);
                    detailIntent.putExtra("tenantId", tenant.getId());
                    detailIntent.putExtra("tenantNama", tenant.getNama());
                    detailIntent.putExtra("tenantGambar", tenant.getGambar());
                    detailIntent.putExtra("tenantKategori", tenant.getKategori());
                    detailIntent.putExtra("tenantDeskripsi", tenant.getDeskripsi());

                    SharedPreferences prefs = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
                    String orderMode = prefs.getString("orderMode", "TAKE_AWAY");
                    String mejaId = prefs.getString("mejaId", "");
                    detailIntent.putExtra("orderMode", orderMode);
                    detailIntent.putExtra("mejaId", mejaId);
                    startActivity(detailIntent);
                }, userId
        );
        rvTenants.setLayoutManager(new LinearLayoutManager(this));
        rvTenants.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.ivMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.menuBeranda).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        // Ganti yang sebelumnya hanya toast
        findViewById(R.id.menuProfil).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
        });
        findViewById(R.id.menuTenantTersimpan).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HomeActivity.this, SavedTenantsActivity.class));
        });
        findViewById(R.id.menuKategori).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HomeActivity.this, CategoryActivity.class));
        });
        findViewById(R.id.menuTentang).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Tampilkan dialog tentang
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Tentang Aplikasi")
                    .setMessage("FoodCourt Go v1.0\nAplikasi pemesanan makanan foodcourt UNM")
                    .setPositiveButton("OK", null)
                    .show();
        });
        findViewById(R.id.menuBantuan).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Bantuan")
                    .setMessage("Hubungi admin: foodcourt@unm.ac.id\nAtau datang ke kantor foodcourt")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        muatSemuaTenant();
    }

    private void muatSemuaTenant() {
        DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tampilkanEmptyState(true);
                    return;
                }
                for (DataSnapshot child : snapshot.getChildren()) {
                    TenantModel tenant = child.getValue(TenantModel.class);
                    if (tenant != null) {
                        tenant.setId(child.getKey());
                        tenantList.add(tenant);
                    }
                }
                tenantListFiltered.clear();
                tenantListFiltered.addAll(tenantList);
                adapter.notifyDataSetChanged();
                tampilkanEmptyState(tenantListFiltered.isEmpty());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String keyword) {
        tenantListFiltered.clear();
        if (keyword.isEmpty()) {
            tenantListFiltered.addAll(tenantList);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (TenantModel tenant : tenantList) {
                if ((tenant.getNama() != null && tenant.getNama().toLowerCase().contains(lowerKeyword)) ||
                        (tenant.getKategori() != null && tenant.getKategori().toLowerCase().contains(lowerKeyword)) ||
                        (tenant.getDeskripsi() != null && tenant.getDeskripsi().toLowerCase().contains(lowerKeyword))) {
                    tenantListFiltered.add(tenant);
                }
            }
        }
        adapter.notifyDataSetChanged();
        tampilkanEmptyState(tenantListFiltered.isEmpty());
    }

    private void tampilkanDialogLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari akun ini?")
                .setPositiveButton("Ya, Logout", (dialog, which) -> {
                    getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void tampilkanEmptyState(boolean isEmpty) {
        llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTenants.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}