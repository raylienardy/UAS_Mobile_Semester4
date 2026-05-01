package com.example.foodcourtgo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetailTenantActivity extends AppCompatActivity {

    private ImageView ivBack, ivTenantImage;
    private TextView tvTenantNama, tvTenantKategori, tvTenantDeskripsi;
    private EditText etSearchMenu;
    private RecyclerView rvMenu;

    private LinearLayout llOrderBar;
    private TextView tvOrderItemCount, tvOrderTenantName, tvOrderTotalHarga;

    private MenuAdapter menuAdapter;
    private List<MenuModel> menuList = new ArrayList<>();
    private List<MenuModel> menuListFiltered = new ArrayList<>();
    private List<PesananItem> pesananList = new ArrayList<>();   // ganti Map

    private String tenantId, tenantNama, tenantGambar, tenantKategori, tenantDeskripsi;

    private ActivityResultLauncher<Intent> menuOptionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String id = data.getStringExtra("menuId");
                    String nama = data.getStringExtra("menuNama");
                    long harga = data.getLongExtra("menuHarga", 0);
                    String opsi = data.getStringExtra("opsi");
                    long tambahan = data.getLongExtra("hargaTambahan", 0);

                    PesananItem item = new PesananItem(id, nama, harga, opsi, tambahan);
                    pesananList.add(item);
                    updateOrderBar();
                }
            });

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

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter(this, menuListFiltered,
                (menu, position) -> {
                    Intent optIntent = new Intent(DetailTenantActivity.this, MenuOptionActivity.class);
                    optIntent.putExtra("menuId", menu.getMenuId());
                    menuOptionLauncher.launch(optIntent);
                }
        );
        rvMenu.setAdapter(menuAdapter);

        etSearchMenu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenu(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        ivBack.setOnClickListener(v -> finish());

        LinearLayout orderButton = findViewById(R.id.order_button);
        if (orderButton != null) {
            orderButton.setOnClickListener(v -> tampilkanRingkasan());
        } else {
            llOrderBar.setOnClickListener(v -> tampilkanRingkasan());
        }

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

    private void updateOrderBar() {
        if (pesananList.isEmpty()) {
            llOrderBar.setVisibility(View.GONE);
            return;
        }
        llOrderBar.setVisibility(View.VISIBLE);

        int totalItems = pesananList.size();
        long totalHarga = 0;
        for (PesananItem item : pesananList) {
            totalHarga += item.getTotalHarga();
        }

        tvOrderItemCount.setText(totalItems + " item");
        tvOrderTenantName.setText(tenantNama);
        tvOrderTotalHarga.setText("Rp" + String.format("%,d", totalHarga).replace(',', '.'));
    }

    private void tampilkanRingkasan() {
        if (pesananList.isEmpty()) {
            Toast.makeText(this, "Belum ada pesanan", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan pesanan ke holder (global)
        PesananHolder.getPesananList().clear();
        PesananHolder.getPesananList().addAll(pesananList);

        // Tampilkan dialog dengan opsi checkout
        new AlertDialog.Builder(this)
                .setTitle("Pesanan Anda")
                .setMessage("Total: Rp" + String.format("%,d", hitungTotalHarga()).replace(',', '.') + "\n\nIngin melanjutkan ke pembayaran?")
                .setPositiveButton("Checkout", (dialog, which) -> {
                    startActivity(new Intent(DetailTenantActivity.this, PaymentActivity.class));
                })
                .setNegativeButton("Tutup", null)
                .show();
    }

    private long hitungTotalHarga() {
        long total = 0;
        for (PesananItem item : pesananList) total += item.getTotalHarga();
        return total;
    }
}