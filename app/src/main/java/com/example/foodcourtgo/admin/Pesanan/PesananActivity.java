package com.example.foodcourtgo.admin.Pesanan;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.admin.AkunManagement.AkunManagementActivity;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.admin.LoadingOut.LoadingOutActivity;
import com.example.foodcourtgo.admin.MejaManagement.MejaManagementActivity;
import com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity;
import com.example.foodcourtgo.adapter.PesananAdminAdapter;
import com.example.foodcourtgo.admin.ProfilAdmin.ProfilAdminActivity;
import com.example.foodcourtgo.admin.TenantManagement.TenantManagementActivity;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class PesananActivity extends AppCompatActivity {

    // ── Daftar pesanan ──────────────────────────────
    private RecyclerView rvPesanan;                    // RecyclerView tempat daftar pesanan
    private PesananAdminAdapter adapter;               // Adapter khusus admin untuk menampilkan pesanan
    private List<PesananAdminModel> semuaPesanan = new ArrayList<>();  // List asli semua pesanan dari Firebase
    private DatabaseReference pesananRef;              // Referensi ke node "pesanan" di Firebase
    private String currentFilter = "semua";            // Filter aktif: "semua", "pending", "done" (bisa tambah "processing")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_pesanan);   // Layout halaman pesanan

        // ── Inisialisasi view ───────────────────────
        rvPesanan = findViewById(R.id.rv_pesanan_list);
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");

        // ── Siapkan RecyclerView ────────────────────
        rvPesanan.setLayoutManager(new LinearLayoutManager(this));

        // Inisialisasi adapter dengan list kosong dulu, data di-load nanti
        adapter = new PesananAdminAdapter(new ArrayList<>(), pesanan -> {
            // Ketika item pesanan diklik, buka halaman detail pesanan
            Intent intent = new Intent(PesananActivity.this, DetailPesananActivity.class);
            intent.putExtra("pesananId", pesanan.getId()); // Kirim ID pesanan
            startActivity(intent);
        });
        rvPesanan.setAdapter(adapter);

        // ── Muat data dari Firebase ─────────────────
        loadPesanan();

        // ═════════════════════════════════════════════
        // Filter tombol (Semua, Pending, Selesai)
        // ═════════════════════════════════════════════
        findViewById(R.id.filter_today).setOnClickListener(v -> {
            currentFilter = "semua";   // Tampilkan semua pesanan
            applyFilter();            // Jalankan filter
        });
        findViewById(R.id.filter_pending).setOnClickListener(v -> {
            currentFilter = "pending"; // Tampilkan hanya yang pending
            applyFilter();
        });
        findViewById(R.id.filter_done).setOnClickListener(v -> {
            currentFilter = "done";    // Tampilkan yang sudah selesai
            applyFilter();
        });

        // ═════════════════════════════════════════════
        // Bottom Navigation
        // ═════════════════════════════════════════════
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> {}); // Halaman ini
        findViewById(R.id.btn_quick_akun).setOnClickListener(v -> startActivity(new Intent(this, AkunManagementActivity.class)));


        // Tombol "Meja" → ke halaman manajemen meja
        findViewById(R.id.btn_quick_meja).setOnClickListener(v ->
                startActivity(new Intent(this, MejaManagementActivity.class)));
    }

    /**
     * Ambil semua data pesanan dari Firebase secara realtime.
     * Setiap perubahan di node "pesanan" akan memanggil ulang onDataChange.
     */
    private void loadPesanan() {
        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                semuaPesanan.clear();
                // Iterasi setiap child (setiap pesanan)
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PesananAdminModel pesanan = snap.getValue(PesananAdminModel.class);
                    if (pesanan != null) {
                        pesanan.setId(snap.getKey()); // Simpan key sebagai ID pesanan
                        semuaPesanan.add(pesanan);
                    }
                }
                // Terapkan filter yang sedang aktif
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Bisa ditambahkan Toast jika gagal
            }
        });
    }

    /**
     * Menerapkan filter status pada daftar pesanan.
     * Jika filter "semua" → tampilkan semua.
     * Jika filter "pending" atau "done" → tampilkan yang sesuai.
     */
    private void applyFilter() {
        List<PesananAdminModel> filtered = new ArrayList<>();
        for (PesananAdminModel p : semuaPesanan) {
            // Jika filter "semua" atau status pesanan sesuai dengan filter
            if (currentFilter.equals("semua") || p.getStatus().equals(currentFilter)) {
                filtered.add(p);
            }
        }
        // Kirim data yang sudah difilter ke adapter
        adapter.updateList(filtered);
    }
}