package com.example.foodcourtgo.admin.LoadingOut;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.TenantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class LoadingOutActivity extends AppCompatActivity {

    private Spinner spinnerTenant, spinnerLokasi;
    private Button btnPindah;
    private DatabaseReference tenantRef;
    private List<TenantModel> tenantList = new ArrayList<>();
    private List<String> tenantNames = new ArrayList<>();
    private List<String> tenantIds = new ArrayList<>();
    private List<String> lokasiOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_loading_out);

        spinnerTenant = findViewById(R.id.spinner_tenant);
        spinnerLokasi = findViewById(R.id.spinner_lokasi);
        btnPindah = findViewById(R.id.btn_pindah_tenant);
        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");

        // Ambil data tenant
        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantNames.clear();
                tenantIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    TenantModel tenant = snap.getValue(TenantModel.class);
                    if (tenant != null) {
                        tenant.setId(snap.getKey());
                        tenantList.add(tenant);
                        tenantNames.add(tenant.getNama());
                        tenantIds.add(tenant.getId());
                        // Kumpulkan opsi lokasi unik
                        String lokasi = tenant.getLokasi();
                        if (lokasi != null && !lokasiOptions.contains(lokasi)) {
                            lokasiOptions.add(lokasi);
                        }
                    }
                }
                ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(LoadingOutActivity.this,
                        android.R.layout.simple_spinner_item, tenantNames);
                tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTenant.setAdapter(tenantAdapter);

                ArrayAdapter<String> lokasiAdapter = new ArrayAdapter<>(LoadingOutActivity.this,
                        android.R.layout.simple_spinner_item, lokasiOptions);
                lokasiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLokasi.setAdapter(lokasiAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnPindah.setOnClickListener(v -> {
            int pos = spinnerTenant.getSelectedItemPosition();
            if (pos >= 0 && pos < tenantIds.size()) {
                String tenantId = tenantIds.get(pos);
                String lokasiBaru = spinnerLokasi.getSelectedItem().toString();
                tenantRef.child(tenantId).child("lokasi").setValue(lokasiBaru)
                        .addOnSuccessListener(unused -> Toast.makeText(this, "Tenant dipindahkan ke " + lokasiBaru, Toast.LENGTH_SHORT).show());
            }
        });

        findViewById(R.id.btn_back_loading).setOnClickListener(v -> finish());
    }
}