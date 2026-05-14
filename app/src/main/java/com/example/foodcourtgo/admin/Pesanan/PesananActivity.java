package com.example.foodcourtgo.admin.Pesanan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.admin.AkunManagement.AkunManagementActivity;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
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

    private RecyclerView rvPesanan;
    private PesananAdminAdapter adapter;
    private List<PesananAdminModel> semuaPesanan = new ArrayList<>();
    private DatabaseReference pesananRef;
    private String currentFilter = "semua";
    private TextView filterSemua, filterPending, filterDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_pesanan);

        rvPesanan = findViewById(R.id.rv_pesanan_list);
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");

        rvPesanan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PesananAdminAdapter(new ArrayList<>(), pesanan -> {
            Intent intent = new Intent(PesananActivity.this, DetailPesananActivity.class);
            intent.putExtra("pesananId", pesanan.getId());
            startActivity(intent);
        });
        rvPesanan.setAdapter(adapter);

        // Inisialisasi tombol filter
        filterSemua = findViewById(R.id.filter_today);
        filterPending = findViewById(R.id.filter_pending);
        filterDone = findViewById(R.id.filter_done);

        filterSemua.setOnClickListener(v -> {
            currentFilter = "semua";
            applyFilter();
            updateFilterHighlight();
        });
        filterPending.setOnClickListener(v -> {
            currentFilter = "pending";
            applyFilter();
            updateFilterHighlight();
        });
        filterDone.setOnClickListener(v -> {
            currentFilter = "done";
            applyFilter();
            updateFilterHighlight();
        });

        loadPesanan();

        // Bottom navigation
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> {});
        findViewById(R.id.btn_quick_akun).setOnClickListener(v -> startActivity(new Intent(this, AkunManagementActivity.class)));
        findViewById(R.id.btn_quick_meja).setOnClickListener(v -> startActivity(new Intent(this, MejaManagementActivity.class)));

        // Set highlight awal
        updateFilterHighlight();
    }

    private void loadPesanan() {
        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                semuaPesanan.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PesananAdminModel pesanan = snap.getValue(PesananAdminModel.class);
                    if (pesanan != null) {
                        pesanan.setId(snap.getKey());
                        semuaPesanan.add(pesanan);
                    }
                }
                applyFilter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void applyFilter() {
        List<PesananAdminModel> filtered = new ArrayList<>();
        for (PesananAdminModel p : semuaPesanan) {
            if (currentFilter.equals("semua") || p.getStatus().equals(currentFilter)) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    private void updateFilterHighlight() {
        // Reset semua tombol ke tampilan default (bg_card, teks gelap)
        filterSemua.setBackgroundResource(R.drawable.bg_card);
        filterPending.setBackgroundResource(R.drawable.bg_card);
        filterDone.setBackgroundResource(R.drawable.bg_card);
        filterSemua.setTextColor(getColor(R.color.dark_700));
        filterPending.setTextColor(getColor(R.color.dark_700));
        filterDone.setTextColor(getColor(R.color.dark_700));

        // Highlight tombol yang aktif
        if (currentFilter.equals("semua")) {
            filterSemua.setBackgroundResource(R.drawable.bg_nav_active);
            filterSemua.setTextColor(getColor(R.color.blue_700));
        } else if (currentFilter.equals("pending")) {
            filterPending.setBackgroundResource(R.drawable.bg_nav_active);
            filterPending.setTextColor(getColor(R.color.blue_700));
        } else if (currentFilter.equals("done")) {
            filterDone.setBackgroundResource(R.drawable.bg_nav_active);
            filterDone.setTextColor(getColor(R.color.blue_700));
        }
    }
}