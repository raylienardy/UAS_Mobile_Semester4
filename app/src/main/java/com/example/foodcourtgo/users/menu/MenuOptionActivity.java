package com.example.foodcourtgo.users.menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.TambahanModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MenuOptionActivity extends AppCompatActivity {

    // ── View untuk menampilkan gambar, nama, deskripsi, harga menu ──
    private ImageView ivBack, ivGambar;         // Tombol kembali, gambar menu
    private TextView tvNama, tvDeskripsi, tvHarga; // Teks nama, deskripsi, harga menu
    private LinearLayout llOpsiContainer;       // Tempat menaruh checkbox tambahan secara dinamis
    private Button btnPilih;                    // Tombol "Pilih" untuk konfirmasi opsi

    // ── Data menu yang diterima ─────────────────────
    private String menuId;
    private String menuNama, menuDeskripsi, menuGambar;
    private long menuHarga;                     // Harga dasar menu (long)

    // ── Daftar opsi tambahan yang diambil dari Firebase ──
    private List<TambahanModel> listTambahan;
    // ── Menyimpan semua CheckBox yang dibuat agar bisa dicek saat tombol "Pilih" ditekan ──
    private List<CheckBox> checkBoxList = new ArrayList<>();

    @SuppressLint("MissingInflatedId") // Menekan peringatan jika ada ID yang belum diinflate (opsional)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan dengan file layout XML yang sudah dibuat
        setContentView(R.layout.users_menu_activity_menu_option);

        // ── Inisialisasi view dari layout ─────────────
        ivBack = findViewById(R.id.ivBack);
        ivGambar = findViewById(R.id.ivMenuImage);
        tvNama = findViewById(R.id.tvMenuNama);
        tvDeskripsi = findViewById(R.id.tvMenuDeskripsi);
        tvHarga = findViewById(R.id.tvMenuHarga);
        llOpsiContainer = findViewById(R.id.llOpsiContainer);
        btnPilih = findViewById(R.id.btnPilih);

        // Ambil menuId yang dikirim dari DetailTenantActivity
        menuId = getIntent().getStringExtra("menuId");

        // ── Ambil data detail menu dari Firebase ──────
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference("menu").child(menuId);
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Jika data menu ditemukan
                if (snapshot.exists()) {
                    // Ambil nilai setiap atribut menu
                    menuNama = snapshot.child("nama").getValue(String.class);
                    menuDeskripsi = snapshot.child("deskripsi").getValue(String.class);
                    menuGambar = snapshot.child("gambar").getValue(String.class);
                    // Harga disimpan sebagai Long di Firebase
                    menuHarga = snapshot.child("harga").getValue(Long.class);

                    // Tampilkan data ke layar
                    tvNama.setText(menuNama);
                    tvDeskripsi.setText(menuDeskripsi);
                    // Format harga dengan pemisah ribuan
                    tvHarga.setText("Rp" + String.format("%,d", menuHarga).replace(',', '.'));
                    // Muat gambar menggunakan Glide
                    Glide.with(MenuOptionActivity.this).load(menuGambar).into(ivGambar);

                    // ── Ambil daftar tambahan (opsi) dari node "tambahan" ──
                    listTambahan = new ArrayList<>();
                    for (DataSnapshot child : snapshot.child("tambahan").getChildren()) {
                        TambahanModel t = child.getValue(TambahanModel.class);
                        if (t != null) listTambahan.add(t);
                    }
                    // Buat checkbox sesuai data tambahan
                    buatCheckBoxTambahan();
                } else {
                    // Jika data menu tidak ditemukan, tampilkan pesan dan tutup
                    Toast.makeText(MenuOptionActivity.this, "Menu tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gagal mengambil data dari Firebase
                Toast.makeText(MenuOptionActivity.this, "Gagal memuat menu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // ── Klik tombol kembali ────────────────────────
        // Menutup activity ini tanpa mengirim hasil apa pun (kembali ke DetailTenantActivity)
        ivBack.setOnClickListener(v -> finish());

        // ── Klik tombol "Pilih" ─────────────────────────
        // Mengumpulkan opsi yang dipilih (checkbox dicentang) lalu mengirimkannya kembali ke DetailTenantActivity
        btnPilih.setOnClickListener(v -> {
            StringBuilder opsi = new StringBuilder();
            long totalTambahan = 0;

            // Iterasi semua checkbox yang sudah dibuat
            for (CheckBox cb : checkBoxList) {
                if (cb.isChecked()) {
                    // Ambil objek TambahanModel yang disimpan sebagai tag checkbox
                    TambahanModel t = (TambahanModel) cb.getTag();
                    // Gabungkan nama opsi dengan koma
                    if (opsi.length() > 0) opsi.append(", ");
                    opsi.append(t.getNama());
                    // Tambahkan harga tambahan
                    totalTambahan += t.getHarga();
                }
            }

            // Siapkan intent untuk mengirim hasil kembali ke DetailTenantActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("menuId", menuId);
            resultIntent.putExtra("menuNama", menuNama);
            resultIntent.putExtra("menuHarga", menuHarga);
            resultIntent.putExtra("opsi", opsi.toString());           // Contoh: "Level Pedas, Extra Keju"
            resultIntent.putExtra("hargaTambahan", totalTambahan);   // Total harga tambahan dari opsi
            setResult(RESULT_OK, resultIntent);   // Kirim hasil dengan kode OK
            finish();                             // Tutup activity ini dan kembali ke pemanggil
        });
    }

    /**
     * Membuat CheckBox secara dinamis berdasarkan data tambahan yang diambil dari Firebase.
     * CheckBox akan ditambahkan ke llOpsiContainer.
     */
    private void buatCheckBoxTambahan() {
        llOpsiContainer.removeAllViews();  // Hapus semua view sebelumnya (jika ada)
        checkBoxList.clear();              // Kosongkan list checkbox
        if (listTambahan != null) {
            for (TambahanModel t : listTambahan) {
                // Buat CheckBox baru
                CheckBox cb = new CheckBox(this);
                // Tentukan teks label: nama + harga (jika > 0 tampilkan +Rp..., jika 0 tampilkan Free)
                String label = t.getNama();
                if (t.getHarga() > 0) {
                    label += " (+Rp" + String.format("%,d", t.getHarga()).replace(',', '.') + ")";
                } else {
                    label += " (Free)";
                }
                cb.setText(label);
                cb.setTag(t);  // Simpan objek TambahanModel sebagai tag
                checkBoxList.add(cb);          // Masukkan ke list untuk digunakan nanti
                llOpsiContainer.addView(cb);  // Tambahkan CheckBox ke layout container
            }
        }
    }
}