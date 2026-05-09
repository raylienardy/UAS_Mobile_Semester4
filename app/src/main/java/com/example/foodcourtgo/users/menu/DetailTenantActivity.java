package com.example.foodcourtgo.users.menu;

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
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity.MenuAdapter;
import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity.MenuModel;
import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity_PaymentActivity.PesananHolder;
import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity_PaymentActivity.PesananItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetailTenantActivity extends AppCompatActivity {

    // ── View untuk bagian atas (navbar dan info tenant) ──
    private ImageView ivBack, ivTenantImage;          // Tombol kembali, gambar tenant
    private TextView tvTenantNama, tvTenantKategori, tvTenantDeskripsi; // Nama, kategori, deskripsi tenant

    // ── View untuk pencarian dan daftar menu ──────────
    private EditText etSearchMenu;                    // Kolom pencarian menu
    private RecyclerView rvMenu;                      // Daftar menu (RecyclerView)

    // ── View untuk bar pesanan di bagian bawah ───────
    private LinearLayout llOrderBar;                  // Bar pesanan (muncul jika ada item)
    private TextView tvOrderItemCount;                // Jumlah item
    private TextView tvOrderTenantName;               // Nama tenant di bar pesanan
    private TextView tvOrderTotalHarga;               // Total harga pesanan

    // ── Adapter dan list untuk menu ──────────────────
    private MenuAdapter menuAdapter;                  // Adapter RecyclerView menu
    private List<MenuModel> menuList = new ArrayList<>();           // List menu dari Firebase (asli)
    private List<MenuModel> menuListFiltered = new ArrayList<>();   // List menu yang sudah difilter
    private List<PesananItem> pesananList = new ArrayList<>();      // Daftar pesanan yang dipilih

    // ── Data tenant yang dikirim dari halaman sebelumnya ──
    private String tenantId, tenantNama, tenantGambar, tenantKategori, tenantDeskripsi;

    // ── ActivityResultLauncher untuk menangkap opsi menu ketika memilih menu ──
    // Setelah user memilih opsi di MenuOptionActivity, hasil dikembalikan ke sini
    private ActivityResultLauncher<Intent> menuOptionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Cek apakah MenuOptionActivity mengirim hasil dengan kode OK
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    // Ambil data menu yang dikirim
                    String id = data.getStringExtra("menuId");
                    String nama = data.getStringExtra("menuNama");
                    long harga = data.getLongExtra("menuHarga", 0);
                    String opsi = data.getStringExtra("opsi");          // Opsi tambahan (misal level pedas)
                    long tambahan = data.getLongExtra("hargaTambahan", 0);  // Harga tambahan dari opsi

                    // Buat objek PesananItem baru dan tambahkan ke daftar pesanan
                    PesananItem item = new PesananItem(id, nama, harga, opsi, tambahan);
                    pesananList.add(item);
                    // Perbarui tampilan bar pesanan di bawah
                    updateOrderBar();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan activity dengan file layout XML
        setContentView(R.layout.users_menu_activity_detail_tenant);

        // ── Inisialisasi semua view dari layout ────────
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

        // ── Ambil data tenant yang dikirim via Intent ──
        Intent intent = getIntent();
        tenantId = intent.getStringExtra("tenantId");
        tenantNama = intent.getStringExtra("tenantNama");
        tenantGambar = intent.getStringExtra("tenantGambar");
        tenantKategori = intent.getStringExtra("tenantKategori");
        tenantDeskripsi = intent.getStringExtra("tenantDeskripsi");

        // ── Tampilkan data tenant ke layar ──────────────
        tvTenantNama.setText(tenantNama);
        tvTenantKategori.setText(tenantKategori);
        tvTenantDeskripsi.setText(tenantDeskripsi);
        // Gunakan library Glide untuk memuat gambar tenant dari URL atau resource
        Glide.with(this).load(tenantGambar).into(ivTenantImage);

        // ── Siapkan RecyclerView untuk daftar menu ──────
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        // Inisialisasi adapter; klik pada item menu akan membuka MenuOptionActivity via launcher
        menuAdapter = new MenuAdapter(this, menuListFiltered,
                (menu, position) -> {
                    Intent optIntent = new Intent(DetailTenantActivity.this, MenuOptionActivity.class);
                    optIntent.putExtra("menuId", menu.getMenuId());
                    // Jalankan launcher untuk mendapatkan hasil pilihan opsi
                    menuOptionLauncher.launch(optIntent);
                }
        );
        rvMenu.setAdapter(menuAdapter);

        // ── Pencarian menu ──────────────────────────────
        etSearchMenu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenu(s.toString());  // Panggil filter saat teks berubah
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ── Tombol kembali di navbar ────────────────────
        ivBack.setOnClickListener(v -> finish());  // Tutup activity, kembali ke halaman sebelumnya

        // ── Klik pada bar pesanan (atau tombol "order_button") ──
        LinearLayout orderButton = findViewById(R.id.order_button);
        if (orderButton != null) {
            orderButton.setOnClickListener(v -> tampilkanRingkasan());
        } else {
            // Fallback jika tombol tidak ada
            llOrderBar.setOnClickListener(v -> tampilkanRingkasan());
        }

        // ── Muat data menu dari Firebase ────────────────
        muatMenu();
    }

    /**
     * Mengambil semua menu milik tenant ini dari Firebase Realtime Database
     * Data menu disimpan di node "menu", difilter berdasarkan tenantId
     */
    private void muatMenu() {
        FirebaseDatabase.getInstance().getReference("menu")
                .orderByChild("tenantId").equalTo(tenantId)  // Hanya ambil menu dengan tenantId yang sama
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        menuList.clear();  // Kosongkan list lama
                        // Loop setiap data menu yang ditemukan
                        for (DataSnapshot child : snapshot.getChildren()) {
                            MenuModel menu = child.getValue(MenuModel.class);
                            if (menu != null) {
                                menu.setMenuId(child.getKey());  // Simpan key sebagai ID menu
                                menuList.add(menu);
                            }
                        }
                        // Awalnya, tampilkan semua menu (tanpa filter)
                        menuListFiltered.clear();
                        menuListFiltered.addAll(menuList);
                        menuAdapter.notifyDataSetChanged();  // Beri tahu adapter bahwa data berubah
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetailTenantActivity.this, "Gagal memuat menu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Filter menu berdasarkan keyword yang diketik user di kolom pencarian
     */
    private void filterMenu(String keyword) {
        menuListFiltered.clear();
        if (keyword.isEmpty()) {
            // Tanpa keyword, tampilkan semua
            menuListFiltered.addAll(menuList);
        } else {
            String lower = keyword.toLowerCase();
            for (MenuModel menu : menuList) {
                // Cocokkan dengan nama atau deskripsi menu
                if (menu.getNama().toLowerCase().contains(lower) ||
                        menu.getDeskripsi().toLowerCase().contains(lower)) {
                    menuListFiltered.add(menu);
                }
            }
        }
        menuAdapter.notifyDataSetChanged(); // Perbarui RecyclerView
    }

    /**
     * Perbarui tampilan bar pesanan (jumlah item, nama tenant, total harga)
     */
    private void updateOrderBar() {
        if (pesananList.isEmpty()) {
            llOrderBar.setVisibility(View.GONE);  // Sembunyikan bar jika kosong
            return;
        }
        llOrderBar.setVisibility(View.VISIBLE);  // Tampilkan bar

        int totalItems = pesananList.size();     // Jumlah item yang dipesan
        long totalHarga = 0;
        for (PesananItem item : pesananList) {
            totalHarga += item.getTotalHarga();  // Hitung total semua item
        }

        // Perbarui teks di bar
        tvOrderItemCount.setText(totalItems + " item");
        tvOrderTenantName.setText(tenantNama);
        // Format harga dengan pemisah ribuan, ganti koma jadi titik
        tvOrderTotalHarga.setText("Rp" + String.format("%,d", totalHarga).replace(',', '.'));
    }

    /**
     * Tampilkan dialog ringkasan pesanan dan opsi checkout
     */
    private void tampilkanRingkasan() {
        if (pesananList.isEmpty()) {
            Toast.makeText(this, "Belum ada pesanan", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan pesanan ke holder global agar bisa diakses PaymentActivity
        PesananHolder.getPesananList().clear();
        PesananHolder.getPesananList().addAll(pesananList);

        // Dialog konfirmasi dengan total harga
        new AlertDialog.Builder(this)
                .setTitle("Pesanan Anda")
                .setMessage("Total: Rp" + String.format("%,d", hitungTotalHarga()).replace(',', '.') +
                        "\n\nIngin melanjutkan ke pembayaran?")
                .setPositiveButton("Checkout", (dialog, which) -> {
                    // Pindah ke PaymentActivity dengan membawa tenantId
                    Intent intent = new Intent(DetailTenantActivity.this, PaymentActivity.class);
                    intent.putExtra("tenantId", tenantId);
                    startActivity(intent);
                })
                .setNegativeButton("Tutup", null)
                .show();
    }

    /**
     * Hitung total harga seluruh pesanan
     */
    private long hitungTotalHarga() {
        long total = 0;
        for (PesananItem item : pesananList) total += item.getTotalHarga();
        return total;
    }
}