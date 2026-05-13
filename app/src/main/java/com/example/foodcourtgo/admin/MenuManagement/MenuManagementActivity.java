package com.example.foodcourtgo.admin.MenuManagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.MenuReadOnlyAdapter;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.admin.MejaManagement.MejaManagementActivity;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.ProfilAdminActivity.ProfilAdminActivity;
import com.example.foodcourtgo.admin.TenantManagement.TenantManagementActivity;
import com.example.foodcourtgo.model.MenuModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MenuManagementActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvMenu;
    private MenuReadOnlyAdapter adapter;
    private List<MenuModel> menuList = new ArrayList<>();
    private List<MenuModel> filteredList = new ArrayList<>();
    private DatabaseReference menuRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_menu_management);

        etSearch = findViewById(R.id.et_search_menu);
        rvMenu = findViewById(R.id.rv_menu_list);
        menuRef = FirebaseDatabase.getInstance().getReference("menu");

        // Sembunyikan tombol tambah menu (karena view only)
//        findViewById(R.id.btn_add_menu).setVisibility(android.view.View.GONE);

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuReadOnlyAdapter();
        rvMenu.setAdapter(adapter);

        loadMenu();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Bottom navigation sama seperti sebelumnya
        findViewById(R.id.nav_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v ->
                startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> {});
        // Meja (halaman ini sendiri, tidak perlu pindah)
        findViewById(R.id.btn_quick_meja).setOnClickListener(v ->
                startActivity(new Intent(this, MejaManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v ->
                startActivity(new Intent(this, PesananActivity.class)));
        findViewById(R.id.nav_profil).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilAdminActivity.class)));
    }

    private void loadMenu() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MenuModel menu = snap.getValue(MenuModel.class);
                    if (menu != null) {
                        menu.setMenuId(snap.getKey());
                        menuList.add(menu);
                    }
                }
                filteredList.clear();
                filteredList.addAll(menuList);
                adapter.setMenuList(filteredList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filter(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) {
            filteredList.addAll(menuList);
        } else {
            String lower = keyword.toLowerCase();
            for (MenuModel menu : menuList) {
                if (menu.getNama().toLowerCase().contains(lower) ||
                        menu.getDeskripsi().toLowerCase().contains(lower)) {
                    filteredList.add(menu);
                }
            }
        }
        adapter.setMenuList(filteredList);
    }
}