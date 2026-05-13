package com.example.foodcourtgo.admin.AkunManagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.AkunAdminAdapter;
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.TenantManagement.TenantManagementActivity;
import com.example.foodcourtgo.model.AkunModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AkunManagementActivity extends AppCompatActivity {

    private RecyclerView rvAkun;
    private AkunAdminAdapter adapter;
    private List<AkunModel> akunList = new ArrayList<>();
    private DatabaseReference akunRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_akun_management);

        rvAkun = findViewById(R.id.rv_akun_list);
        rvAkun.setLayoutManager(new LinearLayoutManager(this));

        akunRef = FirebaseDatabase.getInstance().getReference("akun");

        adapter = new AkunAdminAdapter(new AkunAdminAdapter.OnAkunActionListener() {
            @Override
            public void onItemClick(AkunModel akun) {
                // Klik biasa: edit atau lihat detail? Bisa buka dialog untuk reset password atau toggle active
                showAkunOptionsDialog(akun);
            }

            @Override
            public void onLongClick(AkunModel akun) {
                // Long click: reset password cepat
                showResetPasswordDialog(akun);
            }
        });
        rvAkun.setAdapter(adapter);

        loadAkun();

        // ═════════════════════════════════════════════
        // Bottom Navigation
        // ═════════════════════════════════════════════
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> startActivity(new Intent(this, PesananActivity.class))); // Halaman ini
        findViewById(R.id.btn_quick_akun).setOnClickListener(v -> {});

    }

    private void loadAkun() {
        akunRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                akunList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    AkunModel akun = snap.getValue(AkunModel.class);
                    if (akun != null) {
                        akun.setUserId(snap.getKey());
                        akunList.add(akun);
                    }
                }
                adapter.setAkunList(akunList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AkunManagementActivity.this, "Gagal load akun", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAkunOptionsDialog(AkunModel akun) {
        String[] options = {"Reset Password", (akun.isActive() ? "Nonaktifkan" : "Aktifkan")};
        new AlertDialog.Builder(this)
                .setTitle("Kelola Akun: " + akun.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showResetPasswordDialog(akun);
                    } else {
                        toggleActiveStatus(akun);
                    }
                })
                .show();
    }

    private void showResetPasswordDialog(AkunModel akun) {
        EditText input = new EditText(this);
        input.setHint("Masukkan password baru");
        new AlertDialog.Builder(this)
                .setTitle("Reset Password untuk " + akun.getName())
                .setView(input)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newPass = input.getText().toString().trim();
                    if (newPass.isEmpty()) {
                        Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    akunRef.child(akun.getUserId()).child("pass").setValue(newPass)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Password berhasil direset", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void toggleActiveStatus(AkunModel akun) {
        boolean newStatus = !akun.isActive();
        akunRef.child(akun.getUserId()).child("isActive").setValue(newStatus)
                .addOnSuccessListener(unused -> {
                    String msg = newStatus ? "Akun diaktifkan" : "Akun dinonaktifkan";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
    }
}