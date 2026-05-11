package com.example.foodcourtgo.admin.Pesanan;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
// Model item pesanan (nama, qty, harga, opsi) untuk ditampilkan
import com.example.foodcourtgo.addson.ItemPesananModel;
// Model pesanan lengkap yang tersimpan di Firebase
import com.example.foodcourtgo.addson.PesananAdminModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailPesananActivity extends AppCompatActivity {

    // ── Komponen tampilan detail pesanan ────────────
    private TextView tvId, tvTable, tvStatus, tvTenant, tvCustomer, tvPayment, tvItems, tvTotal;

    // ── ID pesanan yang diterima dari halaman sebelumnya ──
    private String pesananId;

    // ── Referensi ke node pesanan tertentu di Firebase ──
    private DatabaseReference pesananRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hubungkan dengan layout detail pesanan (admin)
        setContentView(R.layout.admin_activity_detail_pesanan);

        // Ambil pesananId yang dikirim via Intent (dari PesananActivity)
        pesananId = getIntent().getStringExtra("pesananId");

        // Buat referensi ke node "pesanan/{pesananId}" di Firebase
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan").child(pesananId);

        // ── Inisialisasi semua TextView dari layout ──
        tvId       = findViewById(R.id.tv_detail_order_id);
        tvTable    = findViewById(R.id.tv_detail_table);
        tvStatus   = findViewById(R.id.chip_detail_status);
        tvTenant   = findViewById(R.id.tv_detail_tenant);
        tvCustomer = findViewById(R.id.tv_detail_customer);
        tvPayment  = findViewById(R.id.tv_detail_payment);
        tvItems    = findViewById(R.id.tv_detail_items_title); // akan diisi daftar item
        tvTotal    = findViewById(R.id.tv_detail_total);

        // ── Muat data detail dari Firebase ──────────
        loadDetail();

        // ── Tombol kembali di header (karakter ‹) ────
        findViewById(R.id.btn_back_detail).setOnClickListener(v -> finish());

        // ── Tombol ubah status pesanan ──────────────
        // "Diproses"
        findViewById(R.id.btn_detail_process).setOnClickListener(v -> updateStatus("processing"));
        // "Selesai"
        findViewById(R.id.btn_detail_done).setOnClickListener(v -> updateStatus("done"));
        // "Batalkan"
        findViewById(R.id.btn_detail_cancel).setOnClickListener(v -> updateStatus("cancelled"));
    }

    /**
     * Mengambil data lengkap pesanan dari Firebase (sekali ambil).
     * Data ditampilkan ke semua TextView yang sudah disiapkan.
     */
    private void loadDetail() {
        // addListenerForSingleValueEvent = ambil data sekali saja
        pesananRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Konversi snapshot ke objek PesananAdminModel
                    PesananAdminModel p = snapshot.getValue(PesananAdminModel.class);
                    if (p == null) return;

                    // Isi data utama pesanan
                    tvId.setText("ID Pesanan: " + p.getId());
                    tvTable.setText("Meja " + p.getMeja() + " • " + p.getWaktu());
                    tvStatus.setText(p.getStatus());
                    tvTenant.setText("Tenant: " + p.getTenantNama());
                    tvCustomer.setText("Customer: " + p.getCustomerName());
                    tvPayment.setText("Pembayaran: QRIS"); // Untuk sementara statis

                    // Gabungkan seluruh item pesanan menjadi satu string
                    StringBuilder itemsStr = new StringBuilder();
                    if (p.getItems() != null) {
                        for (ItemPesananModel item : p.getItems()) {
                            // Format: nama   qty x Rp harga
                            itemsStr.append(item.getNama()).append("  ").append(item.getQty())
                                    .append(" x Rp ").append(String.format("%,d", item.getHarga()))
                                    .append("\n");
                        }
                    }
                    tvItems.setText(itemsStr.toString().trim());

                    // Tampilkan total harga dengan pemisah ribuan
                    tvTotal.setText("Total: Rp " + String.format("%,d", p.getTotalHarga()).replace(',', '.'));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Bisa ditambahkan Toast jika gagal
            }
        });
    }

    /**
     * Memperbarui status pesanan di Firebase.
     * Setelah berhasil, muat ulang detail untuk menampilkan status baru.
     */
    private void updateStatus(String status) {
        // Update child "status" pada node pesanan ini
        pesananRef.child("status").setValue(status).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Status diubah menjadi " + status, Toast.LENGTH_SHORT).show();
            loadDetail(); // Perbarui tampilan
        });
    }
}