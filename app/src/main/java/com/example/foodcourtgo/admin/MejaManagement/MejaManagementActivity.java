package com.example.foodcourtgo.admin.MejaManagement;  // sesuaikan dengan package Anda

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.MejaAdminAdapter;
import com.example.foodcourtgo.admin.AkunManagement.AkunManagementActivity;
import com.example.foodcourtgo.admin.LoadingOut.LoadingOutActivity;
import com.example.foodcourtgo.model.MejaModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Import semua activity tujuan bottom nav
import com.example.foodcourtgo.admin.DashboardAdmin.DashboardAdminActivity;
import com.example.foodcourtgo.admin.TenantManagement.TenantManagementActivity;
import com.example.foodcourtgo.admin.MenuManagement.MenuManagementActivity;
import com.example.foodcourtgo.admin.Pesanan.PesananActivity;
import com.example.foodcourtgo.admin.ProfilAdmin.ProfilAdminActivity;

public class MejaManagementActivity extends AppCompatActivity {

    private RecyclerView rvMeja;
    private MejaAdminAdapter adapter;
    private List<MejaModel> mejaList = new ArrayList<>();
    private DatabaseReference mejaRef;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_meja_management);

        rvMeja = findViewById(R.id.rv_meja_list);
        fabAdd = findViewById(R.id.fab_add_meja);
        rvMeja.setLayoutManager(new LinearLayoutManager(this));

        mejaRef = FirebaseDatabase.getInstance().getReference("meja");

        adapter = new MejaAdminAdapter(new MejaAdminAdapter.OnMejaActionListener() {
            @Override
            public void onEditClick(MejaModel meja) {
                Intent intent = new Intent(MejaManagementActivity.this, AdminAddMejaActivity.class);
                intent.putExtra("mejaId", meja.getId());
                intent.putExtra("nomor", meja.getNomor());
                intent.putExtra("lokasi", meja.getLokasi());
                intent.putExtra("status", meja.getStatus());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(MejaModel meja) {
                new AlertDialog.Builder(MejaManagementActivity.this)
                        .setTitle("Hapus Meja")
                        .setMessage("Yakin hapus meja " + meja.getNomor() + "?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            mejaRef.child(meja.getId()).removeValue()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(MejaManagementActivity.this, "Meja dihapus", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }

            @Override
            public void onItemClick(MejaModel meja) {
                // opsional: tampilkan QR dialog
                showQrDialog(meja);
            }
        });
        rvMeja.setAdapter(adapter);

        loadMeja();

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MejaManagementActivity.this, AdminAddMejaActivity.class));
        });

        // Tombol back (header)
        findViewById(R.id.btn_back_meja).setOnClickListener(v -> finish());

        // ========== BOTTOM NAVIGATION HANDLER ==========
        // Dashboard (Home)
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            startActivity(new Intent(MejaManagementActivity.this, DashboardAdminActivity.class));
            finish();
        });
        // Tenant
        findViewById(R.id.nav_tenant).setOnClickListener(v -> {
            startActivity(new Intent(MejaManagementActivity.this, TenantManagementActivity.class));
            finish();
        });
        // Menu
        findViewById(R.id.nav_menu).setOnClickListener(v -> {
            startActivity(new Intent(MejaManagementActivity.this, MenuManagementActivity.class));
            finish();
        });
        // Meja (halaman ini sendiri, tidak perlu pindah)
        findViewById(R.id.nav_meja).setOnClickListener(v -> { /* tetap di sini */ });
        // Order
        findViewById(R.id.nav_pesanan).setOnClickListener(v -> {
            startActivity(new Intent(MejaManagementActivity.this, PesananActivity.class));
            finish();
        });
        // Akun
        findViewById(R.id.btn_quick_akun).setOnClickListener(v -> {
            startActivity(new Intent(this, AkunManagementActivity.class));
            finish();
        });
    }

    private void loadMeja() {
        mejaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mejaList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MejaModel meja = snap.getValue(MejaModel.class);
                    if (meja != null) {
                        meja.setId(snap.getKey());
                        mejaList.add(meja);
                    }
                }
                adapter.setMejaList(mejaList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MejaManagementActivity.this, "Gagal load meja", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDeleteDialog(MejaModel meja) {
        String[] options = {"Edit Meja", "Hapus Meja"};
        new AlertDialog.Builder(this)
                .setTitle("Pilih Aksi")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(MejaManagementActivity.this, AdminAddMejaActivity.class);
                        intent.putExtra("mejaId", meja.getId());
                        intent.putExtra("nomor", meja.getNomor());
                        intent.putExtra("lokasi", meja.getLokasi());
                        intent.putExtra("status", meja.getStatus());
                        startActivity(intent);
                    } else {
                        mejaRef.child(meja.getId()).removeValue()
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(this, "Meja dihapus", Toast.LENGTH_SHORT).show());
                    }
                })
                .show();
    }

    private void showQrDialog(MejaModel meja) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_qr_code, null);
        androidx.appcompat.widget.AppCompatImageView ivQr = view.findViewById(R.id.iv_qr_code);
        TextView tvQrText = view.findViewById(R.id.tv_qr_text);

        String qrData = meja.getQrCode();
        if (qrData == null || qrData.isEmpty()) {
            qrData = "MEJA_" + meja.getNomor() + "_" + System.currentTimeMillis();
            mejaRef.child(meja.getId()).child("qrCode").setValue(qrData);
        }
        tvQrText.setText(qrData);

        // Generate QR Code menggunakan MultiFormatWriter (ZXing)
        try {
            com.google.zxing.MultiFormatWriter writer = new com.google.zxing.MultiFormatWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(400, 400, android.graphics.Bitmap.Config.RGB_565);
            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 400; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }
            ivQr.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            ivQr.setImageResource(android.R.drawable.ic_menu_camera);
        }

        builder.setView(view)
                .setTitle("QR Code Meja " + meja.getNomor())
                .setPositiveButton("Tutup", null)
                .show();
    }
}