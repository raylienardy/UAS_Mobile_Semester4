package com.example.foodcourtgo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.users.menu.addson_PaymentActivity.PesananAdminModel;
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
    private String currentFilter = "semua"; // "semua", "pending", "processing", "done"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesanan);
        // Layout perlu RecyclerView (id rv_pesanan_list)
        rvPesanan = findViewById(R.id.rv_pesanan_list);
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");

        rvPesanan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PesananAdminAdapter(new ArrayList<>(), pesanan -> {
            // Buka detail
            Intent intent = new Intent(PesananActivity.this, DetailPesananActivity.class);
            intent.putExtra("pesananId", pesanan.getId());
            startActivity(intent);
        });
        rvPesanan.setAdapter(adapter);

        loadPesanan();

        // Filter
        findViewById(R.id.filter_today).setOnClickListener(v -> {
            currentFilter = "semua";
            applyFilter();
        });
        findViewById(R.id.filter_pending).setOnClickListener(v -> {
            currentFilter = "pending";
            applyFilter();
        });
        findViewById(R.id.filter_done).setOnClickListener(v -> {
            currentFilter = "done";
            applyFilter();
        });

        // Bottom nav
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> {});
        findViewById(R.id.nav_profil).setOnClickListener(v -> startActivity(new Intent(this, ProfilAdminActivity.class)));
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
            public void onCancelled(@NonNull DatabaseError error) {}
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
}