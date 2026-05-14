package com.example.foodcourtgo.tenant.menu;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.foodcourtgo.tenant.pesanan.TenantOrdersActivity;
import com.example.foodcourtgo.adapter.MenuAdminAdapter;
import com.example.foodcourtgo.model.MenuModel;
import com.example.foodcourtgo.tenant.dashboard.TenantDashboardActivity;
import com.example.foodcourtgo.tenant.profil.TenantProfileActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class TenantMenuActivity extends AppCompatActivity {

    RecyclerView rvMenu;
    MenuAdminAdapter adapter;
    List<MenuModel> menuList = new ArrayList<>();
    List<MenuModel> menuListFull = new ArrayList<>(); // untuk pencarian

    String tenantId;
    DatabaseReference menuRef;
    ValueEventListener menuListener;
    EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_menu);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        rvMenu = findViewById(R.id.rv_menu);
        FloatingActionButton fab = findViewById(R.id.fab_add_menu);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Inisialisasi pencarian
        etSearch = findViewById(R.id.et_search_menu);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenu(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuAdminAdapter(menuList, new MenuAdminAdapter.OnMenuActionListener() {
            @Override
            public void onDelete(MenuModel menu) {
                new AlertDialog.Builder(TenantMenuActivity.this)
                        .setTitle("Hapus Menu")
                        .setMessage("Yakin ingin menghapus " + menu.getNama() + "?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            menuRef.child(menu.getMenuId()).removeValue()
                                    .addOnSuccessListener(u ->
                                            Toast.makeText(TenantMenuActivity.this, "Menu dihapus", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }

            @Override
            public void onEdit(MenuModel menu) {
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

        menuRef = FirebaseDatabase.getInstance().getReference("menu");
        menuListener = menuRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        menuList.clear();
                        menuListFull.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MenuModel menu = ds.getValue(MenuModel.class);
                            if (menu != null) {
                                menu.setMenuId(ds.getKey());
                                menuList.add(menu);
                                menuListFull.add(menu);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });

        fab.setOnClickListener(v -> startActivity(new Intent(this, TenantAddMenuActivity.class)));

        // Bottom Navigation
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, TenantDashboardActivity.class)));
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v ->
                startActivity(new Intent(this, TenantOrdersActivity.class)));
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v -> {});
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v ->
                startActivity(new Intent(this, TenantProfileActivity.class)));
    }

    private void filterMenu(String query) {
        List<MenuModel> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(menuListFull);
        } else {
            String lowerQuery = query.toLowerCase();
            for (MenuModel menu : menuListFull) {
                if (menu.getNama().toLowerCase().contains(lowerQuery) ||
                        (menu.getDeskripsi() != null && menu.getDeskripsi().toLowerCase().contains(lowerQuery))) {
                    filtered.add(menu);
                }
            }
        }
        menuList.clear();
        menuList.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (menuListener != null) menuRef.removeEventListener(menuListener);
    }
}