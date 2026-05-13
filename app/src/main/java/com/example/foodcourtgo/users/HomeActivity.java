package com.example.foodcourtgo.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.example.foodcourtgo.adapter.ActiveOrderAdapter;
import com.example.foodcourtgo.adapter.TenantAdapter;
import com.example.foodcourtgo.login.MainActivity;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.example.foodcourtgo.model.TenantModel;
import com.example.foodcourtgo.users.menu.DetailTenantActivity;
import com.example.foodcourtgo.users.menu.StatusPesananActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvTenants, rvActiveOrders;
    private LinearLayout llEmpty;
    private EditText etSearch;
    private TextView tvSidebarNama, tvSidebarUserId, tvSidebarAvatar;
    private TenantAdapter tenantAdapter;
    private List<TenantModel> tenantList = new ArrayList<>();
    private List<TenantModel> tenantListFiltered = new ArrayList<>();
    private String userId;
    private String namaUser;

    // Untuk pesanan aktif
    private ActiveOrderAdapter activeOrderAdapter;
    private List<PesananAdminModel> activeOrderList = new ArrayList<>();
    private ValueEventListener activeOrderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_home);

        // Simpan data dari Intent (orderMode, mejaId) jika ada
        Intent intent = getIntent();
        if (intent != null) {
            String orderMode = intent.getStringExtra("orderMode");
            String mejaId = intent.getStringExtra("mejaId");
            if (orderMode != null || mejaId != null) {
                SharedPreferences prefs = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                if (orderMode != null) editor.putString("orderMode", orderMode);
                if (mejaId != null) editor.putString("mejaId", mejaId);
                editor.apply();
            }
        }

        // Inisialisasi view
        drawerLayout = findViewById(R.id.drawerLayout);
        rvTenants = findViewById(R.id.rvTenants);
        rvActiveOrders = findViewById(R.id.rvActiveOrders);
        llEmpty = findViewById(R.id.llEmpty);
        etSearch = findViewById(R.id.etSearch);
        // Tangani filter kategori dari CategoryActivity
        String selectedCategory = getIntent().getStringExtra("selectedCategory");
        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            etSearch.setText(selectedCategory);
            filter(selectedCategory);
        }
        tvSidebarNama = findViewById(R.id.tvSidebarNama);
        tvSidebarUserId = findViewById(R.id.tvSidebarUserId);
        tvSidebarAvatar = findViewById(R.id.tvSidebarAvatar);

        // Ambil data user
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

        // Setup adapter tenant
        tenantAdapter = new TenantAdapter(this, tenantListFiltered,
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
        rvTenants.setAdapter(tenantAdapter);

        // Setup adapter untuk daftar pesanan aktif
        activeOrderAdapter = new ActiveOrderAdapter(this, activeOrderList);
        rvActiveOrders.setLayoutManager(new LinearLayoutManager(this));
        rvActiveOrders.setAdapter(activeOrderAdapter);

        // Fitur pencarian tenant
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Sidebar menu
        findViewById(R.id.ivMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.menuBeranda).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
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
        findViewById(R.id.menuLogout).setOnClickListener(v -> tampilkanDialogLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        muatSemuaTenant();
        loadActiveOrders();   // Memuat daftar pesanan aktif
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
                tenantAdapter.notifyDataSetChanged();
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
        tenantAdapter.notifyDataSetChanged();
        tampilkanEmptyState(tenantListFiltered.isEmpty());
    }

    private void tampilkanEmptyState(boolean isEmpty) {
        llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTenants.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Memuat semua pesanan milik customer yang statusnya pending atau processing
     * dan menampilkannya di RecyclerView rvActiveOrders.
     */
    private void loadActiveOrders() {
        if (userId == null || userId.isEmpty()) return;
        if (activeOrderListener != null) {
            FirebaseDatabase.getInstance().getReference("pesanan")
                    .orderByChild("customerId").equalTo(userId)
                    .removeEventListener(activeOrderListener);
        }
        activeOrderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                activeOrderList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PesananAdminModel pesanan = child.getValue(PesananAdminModel.class);
                    if (pesanan != null) {
                        String status = pesanan.getStatus();
                        // Hanya ambil yang pending atau processing
                        if (status.equals("pending") || status.equals("processing")) {
                            // Jika tenantNama tidak tersimpan, kita bisa ambil dari node tenant
                            if (pesanan.getTenantNama() == null && pesanan.getTenantId() != null) {
                                // Optional: fetch tenant name, tapi untuk kecepatan bisa diabaikan dulu
                                pesanan.setTenantNama("Tenant");
                            }
                            activeOrderList.add(pesanan);
                        }
                    }
                }
                // Tampilkan RecyclerView jika ada, sembunyikan jika tidak
                if (activeOrderList.isEmpty()) {
                    rvActiveOrders.setVisibility(View.GONE);
                } else {
                    rvActiveOrders.setVisibility(View.VISIBLE);
                    activeOrderAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                rvActiveOrders.setVisibility(View.GONE);
            }
        };
        FirebaseDatabase.getInstance().getReference("pesanan")
                .orderByChild("customerId").equalTo(userId)
                .addValueEventListener(activeOrderListener);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeOrderListener != null) {
            FirebaseDatabase.getInstance().getReference("pesanan")
                    .orderByChild("customerId").equalTo(userId)
                    .removeEventListener(activeOrderListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String selectedCategory = intent.getStringExtra("selectedCategory");
        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            etSearch.setText(selectedCategory);
            filter(selectedCategory);
        }
    }
}