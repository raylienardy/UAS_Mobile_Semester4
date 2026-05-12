package com.example.foodcourtgo.users;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.CategoryAdapter;
import com.example.foodcourtgo.model.TenantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<String> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_category);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        rvCategories = findViewById(R.id.rvCategories);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        loadCategories();
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("tenant")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        Set<String> set = new HashSet<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            TenantModel tenant = child.getValue(TenantModel.class);
                            if (tenant != null && tenant.getKategori() != null && !tenant.getKategori().isEmpty()) {
                                set.add(tenant.getKategori());
                            }
                        }
                        categoryList.clear();
                        categoryList.addAll(set);
                        if (categoryList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvCategories.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            rvCategories.setVisibility(View.VISIBLE);
                            CategoryAdapter adapter = new CategoryAdapter(categoryList, category -> {
                                // Ketika kategori diklik, bisa filter tenant di HomeActivity
                                // Untuk saat ini hanya toast
                                Toast.makeText(CategoryActivity.this, "Kategori: " + category, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                            rvCategories.setAdapter(adapter);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryActivity.this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}