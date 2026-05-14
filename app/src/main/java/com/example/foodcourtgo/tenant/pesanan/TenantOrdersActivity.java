package com.example.foodcourtgo.tenant.pesanan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.TenantOrderAdapter;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.example.foodcourtgo.tenant.dashboard.TenantDashboardActivity;
import com.example.foodcourtgo.tenant.menu.TenantMenuActivity;
import com.example.foodcourtgo.tenant.profil.TenantProfileActivity;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TenantOrdersActivity extends AppCompatActivity {

    RecyclerView rvOrders;
    TenantOrderAdapter adapter;
    List<PesananAdminModel> allOrders = new ArrayList<>();

    String tenantId;
    DatabaseReference pesananRef;

    TextView tabAll, tabPending, tabProcessing, tabDone;
    TextView tvEmpty;
    String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_activity_tenant_orders);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        rvOrders = findViewById(R.id.rv_tenant_orders);
        tabAll = findViewById(R.id.tab_orders_all);
        tabPending = findViewById(R.id.tab_orders_pending);
        tabProcessing = findViewById(R.id.tab_orders_process);
        tabDone = findViewById(R.id.tab_orders_done);
        tvEmpty = findViewById(R.id.tv_empty);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TenantOrderAdapter(new ArrayList<>(), order -> {
            Intent i = new Intent(this, TenantOrderDetailActivity.class);
            i.putExtra("pesananId", order.getId());
            startActivity(i);
        });
        rvOrders.setAdapter(adapter);

        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        pesananRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allOrders.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PesananAdminModel p = ds.getValue(PesananAdminModel.class);
                            if (p != null) {
                                p.setId(ds.getKey());
                                allOrders.add(p);
                            }
                        }
                        // Sorting descending berdasarkan ID (yang berisi timestamp)
                        Collections.sort(allOrders, (o1, o2) -> o2.getId().compareTo(o1.getId()));
                        applyFilter();
                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });

        // Set default active tab
        setFilter("all");

        tabAll.setOnClickListener(v -> setFilter("all"));
        tabPending.setOnClickListener(v -> setFilter("pending"));
        tabProcessing.setOnClickListener(v -> setFilter("processing"));
        tabDone.setOnClickListener(v -> setFilter("done"));

        findViewById(R.id.btn_back_orders).setOnClickListener(v -> finish());

        // Bottom Navigation
        findViewById(R.id.nav_tenant_dashboard).setOnClickListener(v ->
                startActivity(new Intent(this, TenantDashboardActivity.class)));
        findViewById(R.id.nav_tenant_orders).setOnClickListener(v -> {});
        findViewById(R.id.nav_tenant_menu).setOnClickListener(v ->
                startActivity(new Intent(this, TenantMenuActivity.class)));
        findViewById(R.id.nav_tenant_profile).setOnClickListener(v ->
                startActivity(new Intent(this, TenantProfileActivity.class)));
    }

    private void setFilter(String filter) {
        currentFilter = filter;

        // Reset semua tab ke style tidak aktif
        resetTabStyle(tabAll);
        resetTabStyle(tabPending);
        resetTabStyle(tabProcessing);
        resetTabStyle(tabDone);

        // Set style aktif untuk tab yang dipilih
        switch (filter) {
            case "all":
                setActiveTabStyle(tabAll);
                break;
            case "pending":
                setActiveTabStyle(tabPending);
                break;
            case "processing":
                setActiveTabStyle(tabProcessing);
                break;
            case "done":
                setActiveTabStyle(tabDone);
                break;
        }
        applyFilter();
    }

    private void resetTabStyle(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_card);
        tab.setTextColor(getResources().getColor(R.color.dark_700));
        tab.setTypeface(null, Typeface.NORMAL);
    }

    private void setActiveTabStyle(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_nav_active);
        tab.setTextColor(getResources().getColor(R.color.blue_700));
        tab.setTypeface(null, Typeface.BOLD);
    }

    private void applyFilter() {
        List<PesananAdminModel> filtered = new ArrayList<>();
        for (PesananAdminModel p : allOrders) {
            if (currentFilter.equals("all") || p.getStatus().equals(currentFilter)) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }
}