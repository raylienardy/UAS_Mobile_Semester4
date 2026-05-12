package com.example.foodcourtgo.admin.MenuManagement;

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

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.addson.MenuAdminAdapter;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.ProfilAdminActivity.ProfilAdminActivity;
import com.example.foodcourtgo.admin.TenantManagement.TenantManagementActivity;
import com.example.foodcourtgo.addson.MenuModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MenuManagementActivity extends AppCompatActivity {

    // ── View untuk pencarian dan daftar menu ─────
    private EditText etSearch;                     // Kolom pencarian menu
    private RecyclerView rvMenu;                   // RecyclerView daftar menu

    // ── Adapter, list data, dan referensi Firebase
    private MenuAdminAdapter adapter;              // Adapter admin yang bisa hapus/edit menu
    private List<MenuModel> menuList = new ArrayList<>();        // Semua data menu asli dari Firebase
    private List<MenuModel> filteredList = new ArrayList<>();    // Data menu setelah difilter
    private DatabaseReference menuRef;                            // Referensi ke node "menu" di Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_menu_management);  // Layout manajemen menu

        // ── Inisialisasi view ──────────────────────
        etSearch = findViewById(R.id.et_search_menu);
        rvMenu   = findViewById(R.id.rv_menu_list);
        // Dapatkan referensi Firebase node "menu"
        menuRef  = FirebaseDatabase.getInstance().getReference("menu");

        // ── Siapkan RecyclerView ────────────────────
        rvMenu.setLayoutManager(new LinearLayoutManager(this));

        // Inisialisasi adapter dengan action listener
        // Interface OnMenuActionListener punya dua method: onDelete dan onEdit
        adapter = new MenuAdminAdapter(filteredList, new MenuAdminAdapter.OnMenuActionListener() {
            @Override
            public void onDelete(MenuModel menu) {
                // Hapus menu dari Firebase berdasarkan menuId-nya
                menuRef.child(menu.getMenuId()).removeValue()
                        .addOnSuccessListener(unused ->
                                Toast.makeText(MenuManagementActivity.this, "Menu dihapus", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onEdit(MenuModel menu) {
                // Fitur edit belum dikembangkan, beri tahu user
                Toast.makeText(MenuManagementActivity.this, "Edit menu belum tersedia", Toast.LENGTH_SHORT).show();
            }
        });
        rvMenu.setAdapter(adapter);

        // ── Muat data menu dari Firebase ───────────
        loadMenu();

        // ── Fitur pencarian menu ───────────────────
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());  // Panggil filter setiap ada perubahan teks
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ── Tombol "+ Tambah Menu" ─────────────────
        // Membuka AdminAddMenuActivity untuk menambah menu baru
        findViewById(R.id.btn_add_menu).setOnClickListener(v ->
                startActivity(new Intent(MenuManagementActivity.this, AdminAddMenuActivity.class)));

        // ═══════════════════════════════════════════
        // Bottom Navigation
        // ═══════════════════════════════════════════
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> {}); // Halaman ini, tidak lakukan apa‑apa
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> startActivity(new Intent(this, PesananActivity.class)));
        findViewById(R.id.nav_profil).setOnClickListener(v -> startActivity(new Intent(this, ProfilAdminActivity.class)));
    }

    /**
     * Mengambil semua data menu dari Firebase (node "menu").
     * Data akan otomatis terbarui karena menggunakan addValueEventListener.
     */
    private void loadMenu() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuList.clear();  // Hapus data lama
                // Loop semua data menu
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MenuModel menu = snap.getValue(MenuModel.class);
                    if (menu != null) {
                        menu.setMenuId(snap.getKey());  // Simpan key sebagai ID menu
                        menuList.add(menu);
                    }
                }
                // Salin semua ke daftar yang difilter (awal tanpa filter)
                filteredList.clear();
                filteredList.addAll(menuList);
                adapter.notifyDataSetChanged();   // Perbarui RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Bisa tambahkan Toast jika diperlukan
            }
        });
    }

    /**
     * Memfilter daftar menu berdasarkan kata kunci dari kolom pencarian.
     * Mencocokkan dengan nama atau deskripsi menu.
     */
    private void filter(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) {
            // Tanpa kata kunci, tampilkan semua
            filteredList.addAll(menuList);
        } else {
            String lower = keyword.toLowerCase();
            for (MenuModel menu : menuList) {
                // Cocokkan dengan nama atau deskripsi (diubah ke huruf kecil)
                if (menu.getNama().toLowerCase().contains(lower) ||
                        menu.getDeskripsi().toLowerCase().contains(lower)) {
                    filteredList.add(menu);
                }
            }
        }
        adapter.notifyDataSetChanged(); // Perbarui RecyclerView
    }
}