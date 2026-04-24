package com.example.foodcourtgo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailTenantActivity extends AppCompatActivity {

    private ImageView ivBack, ivTenantImage;
    private TextView tvTenantNama, tvTenantKategori, tvTenantDeskripsi;
    private EditText etSearchMenu;
    private RecyclerView rvMenu;

    // Bar pesanan bawah
    private LinearLayout llOrderBar;
    private TextView tvOrderItemCount, tvOrderTenantName, tvOrderTotalHarga;
    private View btnOrder;

    private MenuAdapter menuAdapter;
    private List<MenuModel> menuList = new ArrayList<>();
    private List<MenuModel> menuListFiltered = new ArrayList<>();
    private Map<String, Integer> pesananMap = new HashMap<>();

    private String tenantId, tenantNama, tenantGambar, tenantKategori, tenantDeskripsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tenant);

        ivBack = findViewById(R.id.ivBack);
        ivTenantImage = findViewById(R.id.ivTenantImage);
        tvTenantNama = findViewById(R.id.tvTenantNama);
        tvTenantKategori = findViewById(R.id.tvTenantKategori);
        tvTenantDeskripsi = findViewById(R.id.tvTenantDeskripsi);
        etSearchMenu = findViewById(R.id.etSearchMenu);
        rvMenu = findViewById(R.id.rvMenu);

        llOrderBar = findViewById(R.id.llOrderBar);
        tvOrderItemCount = findViewById(R.id.tvOrderItemCount);
        tvOrderTenantName = findViewById(R.id.tvOrderTenantName);
        tvOrderTotalHarga = findViewById(R.id.tvOrderTotalHarga);

        // Ambil data tenant
        Intent intent = getIntent();
        tenantId = intent.getStringExtra("tenantId");
        tenantNama = intent.getStringExtra("tenantNama");
        tenantGambar = intent.getStringExtra("tenantGambar");
        tenantKategori = intent.getStringExtra("tenantKategori");
        tenantDeskripsi = intent.getStringExtra("tenantDeskripsi");

        tvTenantNama.setText(tenantNama);
        tvTenantKategori.setText(tenantKategori);
        tvTenantDeskripsi.setText(tenantDeskripsi);
        Glide.with(this).load(tenantGambar).into(ivTenantImage);

        // RecyclerView
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter(this, menuListFiltered, pesananMap,
                (totalItems, map) -> updateOrderBar(totalItems));
        rvMenu.setAdapter(menuAdapter);

        // Search
        etSearchMenu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenu(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Tombol back
        ivBack.setOnClickListener(v -> finish());

        // Klik tombol pesanan (LinearLayout kanan)
        LinearLayout orderButton = findViewById(R.id.order_button);
        if (orderButton != null) {
            orderButton.setOnClickListener(v -> tampilkanRingkasan());
        } else {
            // Fallback (seharusnya tidak terjadi jika id sudah benar)
            llOrderBar.setOnClickListener(v -> tampilkanRingkasan());
        }

        // Muat menu
        muatMenu();
    }

    private void muatMenu() {
        FirebaseDatabase.getInstance().getReference("menu")
                .orderByChild("tenantId").equalTo(tenantId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        menuList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            MenuModel menu = child.getValue(MenuModel.class);
                            if (menu != null) {
                                menu.setMenuId(child.getKey());
                                menuList.add(menu);
                            }
                        }
                        menuListFiltered.clear();
                        menuListFiltered.addAll(menuList);
                        menuAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetailTenantActivity.this, "Gagal memuat menu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterMenu(String keyword) {
        menuListFiltered.clear();
        if (keyword.isEmpty()) {
            menuListFiltered.addAll(menuList);
        } else {
            String lower = keyword.toLowerCase();
            for (MenuModel menu : menuList) {
                if (menu.getNama().toLowerCase().contains(lower) ||
                        menu.getDeskripsi().toLowerCase().contains(lower)) {
                    menuListFiltered.add(menu);
                }
            }
        }
        menuAdapter.notifyDataSetChanged();
    }

    private void updateOrderBar(int totalItems) {
        if (totalItems == 0) {
            llOrderBar.setVisibility(View.GONE);
            return;
        }

        llOrderBar.setVisibility(View.VISIBLE);

        // Hitung total harga
        long totalHarga = 0;
        for (String menuId : pesananMap.keySet()) {
            int qty = pesananMap.get(menuId);
            for (MenuModel menu : menuList) {
                if (menu.getMenuId().equals(menuId)) {
                    totalHarga += menu.getHarga() * qty;
                    break;
                }
            }
        }

        tvOrderItemCount.setText(totalItems + " item");
        tvOrderTenantName.setText(tenantNama);
        tvOrderTotalHarga.setText("Rp" + String.format("%,d", totalHarga).replace(',', '.'));
    }

    private void tampilkanRingkasan() {
        if (pesananMap.isEmpty()) {
            Toast.makeText(this, "Belum ada pesanan", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        long totalHarga = 0;
        for (String menuId : pesananMap.keySet()) {
            int qty = pesananMap.get(menuId);
            for (MenuModel menu : menuList) {
                if (menu.getMenuId().equals(menuId)) {
                    long harga = menu.getHarga();
                    sb.append(menu.getNama()).append("  x").append(qty)
                            .append("  = Rp").append(String.format("%,d", harga * qty).replace(',', '.')).append("\n");
                    totalHarga += harga * qty;
                    break;
                }
            }
        }
        sb.append("\nTotal: Rp").append(String.format("%,d", totalHarga).replace(',', '.'));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ringkasan Pesanan")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}