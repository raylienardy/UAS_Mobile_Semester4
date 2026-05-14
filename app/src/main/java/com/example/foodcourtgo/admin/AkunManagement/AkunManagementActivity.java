package com.example.foodcourtgo.admin.AkunManagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

        // Adapter dengan listener lengkap (edit, hapus, reset, toggle)
        adapter = new AkunAdminAdapter(new AkunAdminAdapter.OnAkunActionListener() {
            @Override
            public void onItemClick(AkunModel akun) {
                // klik item -> tampilkan dialog edit (opsi lengkap)
                showEditAkunDialog(akun);
            }

            @Override
            public void onLongClick(AkunModel akun) {
                // long click -> reset password cepat
                showResetPasswordDialog(akun);
            }

            @Override
            public void onEdit(AkunModel akun) {
                // tombol edit langsung
                showEditAkunDialog(akun);
            }

            @Override
            public void onDelete(AkunModel akun) {
                // hapus akun
                new AlertDialog.Builder(AkunManagementActivity.this)
                        .setTitle("Hapus Akun")
                        .setMessage("Yakin akan menghapus akun " + akun.getName() + "?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            akunRef.child(akun.getUserId()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AkunManagementActivity.this, "Akun dihapus", Toast.LENGTH_SHORT).show();
                                        // Data sudah otomatis terupdate lewat listener
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(AkunManagementActivity.this, "Gagal hapus akun", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }


            public void onToggleActive(AkunModel akun) {
                toggleActiveStatus(akun);
            }

            public void onResetPassword(AkunModel akun) {
                showResetPasswordDialog(akun);
            }
        });
        rvAkun.setAdapter(adapter);

        loadAkun();

        // Bottom navigation
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardAdminActivity.class)));
        findViewById(R.id.nav_tenant).setOnClickListener(v -> startActivity(new Intent(this, TenantManagementActivity.class)));
        findViewById(R.id.nav_menu).setOnClickListener(v -> startActivity(new Intent(this, MenuManagementActivity.class)));
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> startActivity(new Intent(this, PesananActivity.class)));
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

    // Dialog edit akun lengkap (nama, username, email, pass optional)
    private void showEditAkunDialog(AkunModel akun) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Akun: " + akun.getName());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        EditText etName = new EditText(this);
        etName.setHint("Nama");
        etName.setText(akun.getName());
        layout.addView(etName);

        EditText etUsername = new EditText(this);
        etUsername.setHint("Username");
        etUsername.setText(akun.getUsername());
        layout.addView(etUsername);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(akun.getEmail());
        layout.addView(etEmail);

        EditText etPass = new EditText(this);
        etPass.setHint("Password (kosongkan jika tidak diubah)");
        etPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPass);

        builder.setView(layout);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPass = etPass.getText().toString().trim();

            if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newEmail)) {
                Toast.makeText(this, "Nama, username, dan email harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference userRef = akunRef.child(akun.getUserId());
            userRef.child("name").setValue(newName);
            userRef.child("username").setValue(newUsername);
            userRef.child("email").setValue(newEmail);
            if (!TextUtils.isEmpty(newPass)) {
                userRef.child("pass").setValue(newPass);
            }
            Toast.makeText(this, "Akun diperbarui", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
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