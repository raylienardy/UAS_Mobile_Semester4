package com.example.foodcourtgo;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // Komponen utama
    private DrawerLayout drawerLayout;
    private RecyclerView rvTenants;
    private LinearLayout llEmpty;
    private EditText etSearch;

    // Sidebar views
    private TextView tvSidebarNama, tvSidebarUserId, tvSidebarAvatar;

    // Data
    private TenantAdapter adapter;
    private List<TenantModel> tenantList = new ArrayList<>();         // data asli dari Firebase
    private List<TenantModel> tenantListFiltered = new ArrayList<>(); // data hasil filter
    private String userId;
    private String namaUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Bind semua view
        drawerLayout     = findViewById(R.id.drawerLayout);
        rvTenants        = findViewById(R.id.rvTenants);
        llEmpty          = findViewById(R.id.llEmpty);
        etSearch         = findViewById(R.id.etSearch);
        tvSidebarNama    = findViewById(R.id.tvSidebarNama);
        tvSidebarUserId  = findViewById(R.id.tvSidebarUserId);
        tvSidebarAvatar  = findViewById(R.id.tvSidebarAvatar);

        // Ambil data user dari SharedPreferences
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        userId   = pref.getString("userId", "");
        namaUser = pref.getString("namaUser", "User");

        if (userId.isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Isi header sidebar
        tvSidebarNama.setText(namaUser);
        tvSidebarUserId.setText("ID: " + userId);
        tvSidebarAvatar.setText(namaUser.substring(0, 1).toUpperCase());

        // Di dalam setup adapter (HomeActivity)
        adapter = new TenantAdapter(this, tenantListFiltered,
                tenant -> {
                    Intent intent = new Intent(HomeActivity.this, TakeAwayDineInActivity.class);
                    intent.putExtra("tenantId", tenant.getId());
                    intent.putExtra("tenantNama", tenant.getNama());
                    intent.putExtra("tenantGambar", tenant.getGambar());
                    intent.putExtra("tenantKategori", tenant.getKategori());
                    intent.putExtra("tenantDeskripsi", tenant.getDeskripsi());
                    startActivity(intent);
                }
        );
        rvTenants.setLayoutManager(new LinearLayoutManager(this));
        rvTenants.setAdapter(adapter);

        // ── Search bar ──────────────────────────────
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ── Tombol-tombol navigasi ──────────────────
        findViewById(R.id.ivMenu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // ── Menu sidebar ────────────────────────────
        findViewById(R.id.menuBeranda).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        findViewById(R.id.menuProfil).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Fitur profil segera hadir", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.menuTenantTersimpan).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Ini adalah halaman semua tenant", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.menuKategori).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Fitur kategori segera hadir", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.menuTentang).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "FoodCourt Go v1.0", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.menuBantuan).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Fitur bantuan segera hadir", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.menuLogout).setOnClickListener(v -> tampilkanDialogLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        muatSemuaTenant();
    }

    // ─────────────────────────────────────────────────
    // AMBIL SEMUA TENANT DARI FIREBASE
    // ─────────────────────────────────────────────────
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

                // Tampilkan semua (tanpa filter)
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

    // ─────────────────────────────────────────────────
    // FITUR SEARCH (FILTER LOKAL)
    // ─────────────────────────────────────────────────
    private void filter(String keyword) {
        tenantListFiltered.clear();
        if (keyword.isEmpty()) {
            tenantListFiltered.addAll(tenantList);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (TenantModel tenant : tenantList) {
                if (tenant.getNama() != null && tenant.getNama().toLowerCase().contains(lowerKeyword) ||
                        tenant.getKategori() != null && tenant.getKategori().toLowerCase().contains(lowerKeyword) ||
                        tenant.getDeskripsi() != null && tenant.getDeskripsi().toLowerCase().contains(lowerKeyword)) {
                    tenantListFiltered.add(tenant);
                }
            }
        }
        adapter.notifyDataSetChanged();
        tampilkanEmptyState(tenantListFiltered.isEmpty());
    }

    // ── Dialog Logout ────────────────────────────────
    private void tampilkanDialogLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari akun ini?")
                .setPositiveButton("Ya, Logout", (dialog, which) -> {
                    getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE)
                            .edit().clear().apply();
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