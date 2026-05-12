package com.example.foodcourtgo.tenant.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.tenant.pesanan.TenantOrdersActivity;
import com.example.foodcourtgo.addson.MenuAdminAdapter;
import com.example.foodcourtgo.addson.MenuModel;
import com.example.foodcourtgo.tenant.dashboard.TenantDashboardActivity;
import com.example.foodcourtgo.tenant.profil.TenantProfileActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class TenantMenuActivity extends AppCompatActivity {

    // ── View daftar menu ─────────────────────
    RecyclerView rvMenu;               // RecyclerView untuk menampilkan daftar menu
    MenuAdminAdapter adapter;          // Adapter admin (bisa hapus & edit)
    List<MenuModel> menuList = new ArrayList<>(); // Data menu milik tenant ini

    // ── Data tenant ─────────────────────────
    String tenantId;                   // ID tenant yang sedang login
    DatabaseReference menuRef;         // Referensi Firebase node "menu"
    ValueEventListener menuListener;   // Listener untuk membaca data menu secara realtime

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_menu);   // Layout menu tenant

        // ── Ambil tenantId dari SharedPreferences ──
        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        // ── Inisialisasi view ────────────────────
        rvMenu = findViewById(R.id.rv_menu);
        FloatingActionButton fab = findViewById(R.id.fab_add_menu); // Tombol tambah menu (FAB)
        findViewById(R.id.btn_back).setOnClickListener(v -> finish()); // Tombol kembali

        // ── Setup RecyclerView ────────────────────
        rvMenu.setLayoutManager(new LinearLayoutManager(this));

        // Inisialisasi adapter dengan listener untuk hapus dan edit menu
        adapter = new MenuAdminAdapter(menuList, new MenuAdminAdapter.OnMenuActionListener() {
            @Override
            public void onDelete(MenuModel menu) {
                // Konfirmasi hapus menu
                new AlertDialog.Builder(TenantMenuActivity.this)
                        .setTitle("Hapus Menu")
                        .setMessage("Yakin ingin menghapus " + menu.getNama() + "?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            // Hapus langsung dari Firebase menggunakan menuId
                            menuRef.child(menu.getMenuId()).removeValue()
                                    .addOnSuccessListener(u ->
                                            Toast.makeText(TenantMenuActivity.this, "Menu dihapus", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }

            @Override
            public void onEdit(MenuModel menu) {
                // Buka activity edit menu sambil mengirim data menu saat ini
                Intent intent = new Intent(TenantMenuActivity.this, TenantEditMenuActivity.class);
                intent.putExtra("menuId", menu.getMenuId());
                intent.putExtra("nama", menu.getNama());
                intent.putExtra("deskripsi", menu.getDeskripsi());
                intent.putExtra("harga", menu.getHarga());
                intent.putExtra("gambar", menu.getGambar());
                startActivity(intent);
            }
        });
        rvMenu.setAdapter(adapter);

        // ── Ambil data menu dari Firebase ─────────
        menuRef = FirebaseDatabase.getInstance().getReference("menu");
        // Query hanya menu dengan tenantId yang sama dengan tenant login
        menuListener = menuRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        menuList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MenuModel menu = ds.getValue(MenuModel.class);
                            if (menu != null) {
                                menu.setMenuId(ds.getKey()); // Simpan key sebagai ID menu
                                menuList.add(menu);
                            }
                        }
                        adapter.notifyDataSetChanged();  // Perbarui RecyclerView
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });

        // ── Tombol FAB untuk tambah menu ─────────
        fab.setOnClickListener(v -> startActivity(new Intent(this, TenantAddMenuActivity.class)));

        // ── Bottom Navigation Tenant ─────────────
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, TenantDashboardActivity.class)));
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v -> {}); // Halaman ini, tidak perlu pindah
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v ->
                startActivity(new Intent(this, TenantProfileActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Lepaskan listener Firebase saat activity dihancurkan untuk mencegah memory leak
        if (menuListener != null) menuRef.removeEventListener(menuListener);
    }
}