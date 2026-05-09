package com.example.foodcourtgo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.users.menu.addson_PaymentActivity.ItemPesananModel;
import com.example.foodcourtgo.users.menu.addson_PaymentActivity.PesananAdminModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailPesananActivity extends AppCompatActivity {

    private TextView tvId, tvTable, tvStatus, tvTenant, tvCustomer, tvPayment, tvItems, tvTotal;
    private String pesananId;
    private DatabaseReference pesananRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        XML
        setContentView(R.layout.activity_detail_pesanan);

        pesananId = getIntent().getStringExtra("pesananId");
        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan").child(pesananId);

        tvId = findViewById(R.id.tv_detail_order_id);
        tvTable = findViewById(R.id.tv_detail_table);
        tvStatus = findViewById(R.id.chip_detail_status);
        tvTenant = findViewById(R.id.tv_detail_tenant);
        tvCustomer = findViewById(R.id.tv_detail_customer);
        tvPayment = findViewById(R.id.tv_detail_payment);
        tvItems = findViewById(R.id.tv_detail_items_title); // kita akan gabung di sini
        tvTotal = findViewById(R.id.tv_detail_total);

        loadDetail();

        findViewById(R.id.btn_back_detail).setOnClickListener(v -> finish());
        findViewById(R.id.btn_detail_process).setOnClickListener(v -> updateStatus("processing"));
        findViewById(R.id.btn_detail_done).setOnClickListener(v -> updateStatus("done"));
        findViewById(R.id.btn_detail_cancel).setOnClickListener(v -> updateStatus("cancelled"));
    }

    private void loadDetail() {
        pesananRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PesananAdminModel p = snapshot.getValue(PesananAdminModel.class);
                    if (p == null) return;

                    tvId.setText("ID Pesanan: " + p.getId());
                    tvTable.setText("Meja " + p.getMeja() + " • " + p.getWaktu());
                    tvStatus.setText(p.getStatus());
                    tvTenant.setText("Tenant: " + p.getTenantNama());
                    tvCustomer.setText("Customer: " + p.getCustomerName());
                    tvPayment.setText("Pembayaran: QRIS");

                    StringBuilder itemsStr = new StringBuilder();
                    if (p.getItems() != null) {
                        for (ItemPesananModel item : p.getItems()) {
                            itemsStr.append(item.getNama()).append("  ").append(item.getQty())
                                    .append(" x Rp ").append(String.format("%,d", item.getHarga()))
                                    .append("\n");
                        }
                    }
                    tvItems.setText(itemsStr.toString().trim());
                    tvTotal.setText("Total: Rp " + String.format("%,d", p.getTotalHarga()).replace(',', '.'));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStatus(String status) {
        pesananRef.child("status").setValue(status).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Status diubah menjadi " + status, Toast.LENGTH_SHORT).show();
            loadDetail();
        });
    }
}