package com.example.foodcourtgo.admin.TenantManagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.AkunModel;
import com.example.foodcourtgo.model.TenantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AssignTenantActivity extends AppCompatActivity {

    private Spinner spinnerTenantStand;  // pilih stand (tenant node)
    private Spinner spinnerAkunTenant;   // pilih akun tenant
    private Button btnAssign;

    private DatabaseReference tenantRef;
    private DatabaseReference akunRef;

    private List<TenantModel> tenantList = new ArrayList<>();
    private List<AkunModel> akunList = new ArrayList<>();

    private List<String> tenantNames = new ArrayList<>();
    private List<String> tenantIds = new ArrayList<>();
    private List<String> akunNames = new ArrayList<>();
    private List<String> akunIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_assign_tenant);

        spinnerTenantStand = findViewById(R.id.spinner_tenant_stand);
        spinnerAkunTenant = findViewById(R.id.spinner_akun_tenant);
        btnAssign = findViewById(R.id.btn_assign);

        tenantRef = FirebaseDatabase.getInstance().getReference("tenant");
        akunRef = FirebaseDatabase.getInstance().getReference("akun");

        loadTenantStand();
        loadAkunTenant();

        btnAssign.setOnClickListener(v -> assignTenant());
    }

    private void loadTenantStand() {
        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();
                tenantNames.clear();
                tenantIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    TenantModel tenant = snap.getValue(TenantModel.class);
                    if (tenant != null) {
                        tenant.setId(snap.getKey());
                        // Hanya tampilkan tenant yang belum punya ownerId (belum terassign)
                        if (tenant.getOwnerId() == null || tenant.getOwnerId().isEmpty()) {
                            tenantList.add(tenant);
                            tenantNames.add(tenant.getNama());
                            tenantIds.add(tenant.getId());
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignTenantActivity.this,
                        android.R.layout.simple_spinner_item, tenantNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTenantStand.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignTenantActivity.this, "Gagal load tenant", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAkunTenant() {
        akunRef.orderByChild("role").equalTo("tenant")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        akunList.clear();
                        akunNames.clear();
                        akunIds.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            AkunModel akun = snap.getValue(AkunModel.class);
                            if (akun != null) {
                                akun.setUserId(snap.getKey());
                                // Hanya akun yang belum memiliki tenantId dan isActive true
                                if ((akun.getTenantId() == null || akun.getTenantId().isEmpty()) && akun.isActive()) {
                                    akunList.add(akun);
                                    akunNames.add(akun.getName() + " (" + akun.getUsername() + ")");
                                    akunIds.add(akun.getUserId());
                                }
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignTenantActivity.this,
                                android.R.layout.simple_spinner_item, akunNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerAkunTenant.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AssignTenantActivity.this, "Gagal load akun", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void assignTenant() {
        int tenantPosition = spinnerTenantStand.getSelectedItemPosition();
        int akunPosition = spinnerAkunTenant.getSelectedItemPosition();

        if (tenantPosition < 0 || akunPosition < 0) {
            Toast.makeText(this, "Pilih stand dan akun terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenantId = tenantIds.get(tenantPosition);
        String akunId = akunIds.get(akunPosition);

        // Update node tenant: ownerId = akunId
        tenantRef.child(tenantId).child("ownerId").setValue(akunId);
        // Update node akun: tenantId = tenantId
        akunRef.child(akunId).child("tenantId").setValue(tenantId);

        Toast.makeText(this, "Berhasil assign tenant", Toast.LENGTH_SHORT).show();
        finish(); // kembali ke halaman sebelumnya
    }
}