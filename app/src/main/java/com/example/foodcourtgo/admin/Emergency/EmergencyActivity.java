package com.example.foodcourtgo.admin.Emergency;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.EmergencyAdminAdapter;
import com.example.foodcourtgo.model.EmergencyModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class EmergencyActivity extends AppCompatActivity {

    private RecyclerView rvEmergency;
    private EmergencyAdminAdapter adapter;
    private List<EmergencyModel> emergencyList = new ArrayList<>();
    private DatabaseReference emergencyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_emergency);

        rvEmergency = findViewById(R.id.rv_emergency_list);
        rvEmergency.setLayoutManager(new LinearLayoutManager(this));

        emergencyRef = FirebaseDatabase.getInstance().getReference("emergency");

        adapter = new EmergencyAdminAdapter(emergency -> {
            // Tandai resolved
            emergencyRef.child(emergency.getId()).child("status").setValue("resolved")
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Emergency ditandai selesai", Toast.LENGTH_SHORT).show());
        });
        rvEmergency.setAdapter(adapter);

        loadEmergencies();

        findViewById(R.id.btn_back_emergency).setOnClickListener(v -> finish());
    }

    private void loadEmergencies() {
        emergencyRef.orderByChild("status").equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        emergencyList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            EmergencyModel em = snap.getValue(EmergencyModel.class);
                            if (em != null) {
                                em.setId(snap.getKey());
                                emergencyList.add(em);
                            }
                        }
                        adapter.setList(emergencyList);
                        if (emergencyList.isEmpty()) {
                            Toast.makeText(EmergencyActivity.this, "Tidak ada laporan darurat", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EmergencyActivity.this, "Gagal memuat: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}