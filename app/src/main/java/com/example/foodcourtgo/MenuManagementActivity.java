package com.example.foodcourtgo;

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
    private MenuAdminAdapter adapter;
    private List<MenuModel> menuList = new ArrayList<>();
    private List<MenuModel> filteredList = new ArrayList<>();
    private DatabaseReference menuRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_management);

        etSearch = findViewById(R.id.et_search_menu);
        rvMenu = findViewById(R.id.rv_menu_list);
        menuRef = FirebaseDatabase.getInstance().getReference("menu");

        rvMenu.setLayoutManager(new LinearLayoutManager(this));

        // Perbaiki di sini: gunakan implementasi interface penuh
        adapter = new MenuAdminAdapter(filteredList, new MenuAdminAdapter.OnMenuActionListener() {
            @Override
            public void onDelete(MenuModel menu) {
                // Hapus menu
                menuRef.child(menu.getMenuId()).removeValue()
                        .addOnSuccessListener(unused ->
                                Toast.makeText(MenuManagementActivity.this, "Menu dihapus", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onEdit(MenuModel menu) {
                // Untuk admin, edit bisa diarahkan ke activity yang sama atau diabaikan
                // Jika ingin mengimplementasikan edit, bisa diisi nanti
                Toast.makeText(MenuManagementActivity.this, "Edit menu belum tersedia", Toast.LENGTH_SHORT).show();
            }
        });
        rvMenu.setAdapter(adapter);

        loadMenu();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_add_menu).setOnClickListener(v ->
                startActivity(new Intent(MenuManagementActivity.this, AdminAddMenuActivity.class)));

        // Bottom nav
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> {});
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> startActivity(new Intent(this, PesananActivity.class)));
        findViewById(R.id.nav_profil).setOnClickListener(v -> startActivity(new Intent(this, ProfilAdminActivity.class)));
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filter(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) filteredList.addAll(menuList);
        else {
            String lower = keyword.toLowerCase();
            for (MenuModel menu : menuList) {
                if (menu.getNama().toLowerCase().contains(lower) ||
                        menu.getDeskripsi().toLowerCase().contains(lower)) {
                    filteredList.add(menu);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}