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

// import dulu file2 nya
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.login.MainActivity;

// database
import com.example.foodcourtgo.users.addson_HomeActivity.TenantAdapter;
import com.example.foodcourtgo.users.addson_HomeActivity.TenantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // untuk nanti jalankan sidebar (menu samping)
    private DrawerLayout drawerLayout;
    // daftar tenant akan muncul di sini, tampilannya seperti list yang bisa discroll
    private RecyclerView rvTenants;
    // tampilan kosong kalau tidak ada tenant sama sekali
    private LinearLayout llEmpty;
    // tempat user ngetik buat cari tenant
    private EditText etSearch;

    // yang tampil di bagian atas sidebar: nama, ID, dan inisial avatar
    private TextView tvSidebarNama, tvSidebarUserId, tvSidebarAvatar;

    // adapter: yang mengatur bagaimana setiap tenant ditampilkan di RecyclerView
    private TenantAdapter adapter;
    // simpan semua data tenant yang diambil dari Firebase (asli, belum difilter)
    private List<TenantModel> tenantList = new ArrayList<>();
    // simpan data tenant yang sudah difilter sesuai pencarian user
    private List<TenantModel> tenantListFiltered = new ArrayList<>();
    // ID user yang sedang login, diambil dari SharedPreferences
    private String userId;
    // nama user yang sedang login
    private String namaUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        XML
        // hubungkan dengan layout users_activity_home.xml
        setContentView(R.layout.users_activity_home);

        // tangkap semua view yang ada di layout supaya bisa dipakai di kode
        drawerLayout     = findViewById(R.id.drawerLayout);
        rvTenants        = findViewById(R.id.rvTenants);
        llEmpty          = findViewById(R.id.llEmpty);
        etSearch         = findViewById(R.id.etSearch);
        tvSidebarNama    = findViewById(R.id.tvSidebarNama);
        tvSidebarUserId  = findViewById(R.id.tvSidebarUserId);
        tvSidebarAvatar  = findViewById(R.id.tvSidebarAvatar);

        // ambil data user yang tersimpan (seperti username & ID)
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        userId   = pref.getString("userId", "");
        namaUser = pref.getString("namaUser", "User");

        // kalau userId kosong, artinya belum login → langsung pindah ke halaman login
        if (userId.isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // isi teks di header sidebar dengan data user tadi
        tvSidebarNama.setText(namaUser);
        tvSidebarUserId.setText("ID: " + userId);
        // ambil huruf pertama dari nama buat jadi avatar
        tvSidebarAvatar.setText(namaUser.substring(0, 1).toUpperCase());


//        DISINI SEMUA TAMPILAN TENANT BAKALAN MUNCUL
        // siapkan adapter dan hubungkan dengan RecyclerView
        // kalau tenant diklik, pindah ke halaman TakeAwayDineInActivity sambil bawa data tenant
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

        // ── Fitur search ──────────────────────────────
        // setiap user mengetik di kolom search, langsung filter daftar tenant
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ── Tombol menu samping (garis tiga) ──────────
        findViewById(R.id.ivMenu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START) // buka sidebar dari kiri
        );

        // ── Menu di dalam sidebar ──────────────────────
        // klik Beranda -> tutup sidebar saja (kita sudah di beranda)
        findViewById(R.id.menuBeranda).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

//      ================================   COMING SOON =============================================
        // menu Profil (belum ada halamannya, tampilkan pesan dulu)
        findViewById(R.id.menuProfil).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Fitur profil segera hadir", Toast.LENGTH_SHORT).show();
        });

        // menu Tenant Tersimpan (sementara beri tahu kalau ini xalaman semua tenant)
        findViewById(R.id.menuTenantTersimpan).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Ini adalah halaman semua tenant", Toast.LENGTH_SHORT).show();
        });

        // menu Kategori (belum ada, segera hadir)
        findViewById(R.id.menuKategori).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Fitur kategori segera hadir", Toast.LENGTH_SHORT).show();
        });

        // menu Tentang (tampil versi aplikasi)
        findViewById(R.id.menuTentang).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "FoodCourt Go v1.0", Toast.LENGTH_SHORT).show();
        });

        // menu Bantuan

//       ===========================================
//        ini hapus saja karena tujuannya akan dipindahkan ke halaman tenant
        findViewById(R.id.menuBantuan).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Fitur bantuan segera hadir", Toast.LENGTH_SHORT).show();
        });
//      =================================== COMING SOON ============================================

        // menu Logout -> tampilkan konfirmasi dulu
        findViewById(R.id.menuLogout).setOnClickListener(v -> tampilkanDialogLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // setiap kali halaman ini muncul lagi, muat ulang semua tenant dari Firebase
        muatSemuaTenant();
    }

    // ─────────────────────────────────────────────────
    // Ambil semua tenant dari Firebase, lalu tampilkan
    // ─────────────────────────────────────────────────
    private void muatSemuaTenant() {
        DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        // ambil data sekali saja (tidak perlu pantau perubahan langsung)
        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();
                // kalau tidak ada data sama sekali, tampilkan layar kosong
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tampilkanEmptyState(true);
                    return;
                }

                // ubah setiap data dari Firebase menjadi objek TenantModel
                for (DataSnapshot child : snapshot.getChildren()) {
                    TenantModel tenant = child.getValue(TenantModel.class);
                    if (tenant != null) {
                        tenant.setId(child.getKey()); // simpan ID dari kunci Firebase
                        tenantList.add(tenant);
                    }
                }

                // masukkan semua tenant yang asli ke daftar yang akan ditampilkan (belum difilter)
                tenantListFiltered.clear();
                tenantListFiltered.addAll(tenantList);
                adapter.notifyDataSetChanged(); // beri tahu adapter bahwa data sudah berubah
                tampilkanEmptyState(tenantListFiltered.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // tampilkan pesan kalau gagal mengambil data
                Toast.makeText(HomeActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────────
    // Fitur pencarian tenant secara lokal
    // ─────────────────────────────────────────────────
    private void filter(String keyword) {
        tenantListFiltered.clear();
        if (keyword.isEmpty()) {
            // kalau kolom kosong, tampilkan semua tenant
            tenantListFiltered.addAll(tenantList);
        } else {
            // ubah keyword dan data tenant ke huruf kecil agar pencarian tidak case-sensitive
            String lowerKeyword = keyword.toLowerCase();
            for (TenantModel tenant : tenantList) {
                // cocokkan dengan nama, kategori, atau deskripsi tenant
                if (tenant.getNama() != null && tenant.getNama().toLowerCase().contains(lowerKeyword) ||
                        tenant.getKategori() != null && tenant.getKategori().toLowerCase().contains(lowerKeyword) ||
                        tenant.getDeskripsi() != null && tenant.getDeskripsi().toLowerCase().contains(lowerKeyword)) {
                    tenantListFiltered.add(tenant);
                }
            }
        }
        // perbarui tampilan RecyclerView dan status kosong
        adapter.notifyDataSetChanged();
        tampilkanEmptyState(tenantListFiltered.isEmpty());
    }

    // ── Dialog konfirmasi logout ────────────────────
    private void tampilkanDialogLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari akun ini?")
                .setPositiveButton("Ya, Logout", (dialog, which) -> {
                    // hapus semua data login yang tersimpan
                    getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE)
                            .edit().clear().apply();
                    // pindah ke halaman utama (login) dan tutup semua activity sebelumnya
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Batal", null) // kalau batal, tidak lakukan apa-apa
                .show();
    }

    // tampilkan layar kosong jika tidak ada tenant, atau sembunyikan jika ada
    private void tampilkanEmptyState(boolean isEmpty) {
        llEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTenants.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // saat tombol back ditekan, kalau sidebar kebuka, tutup dulu; kalau tidak, keluar aplikasi
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}