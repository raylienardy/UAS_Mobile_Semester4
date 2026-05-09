package com.example.foodcourtgo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodcourtgo.addson_PaymentActivity_PesananActivity.PesananAdminModel;
import com.google.firebase.database.*;

public class TenantReportActivity extends AppCompatActivity {
    TextView tvTotalIncome, tvTotalOrder, tvDailyAvg;
    String tenantId;
    DatabaseReference pesananRef;
    ValueEventListener reportListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_report);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        tvTotalIncome = findViewById(R.id.tv_report_total_income);
        tvTotalOrder = findViewById(R.id.tv_report_total_order);
        tvDailyAvg = findViewById(R.id.tv_report_daily_average);

        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");
        reportListener = pesananRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long totalIncome = 0;
                        int totalOrders = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PesananAdminModel p = ds.getValue(PesananAdminModel.class);
                            if (p != null && "done".equals(p.getStatus())) {
                                totalIncome += p.getTotalHarga();
                                totalOrders++;
                            }
                        }
                        tvTotalIncome.setText("Rp " + String.format("%,d", totalIncome));
                        tvTotalOrder.setText(String.valueOf(totalOrders));
                        tvDailyAvg.setText("Rp " + (totalOrders > 0 ? String.format("%,d", totalIncome / 30) : "0"));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });

        findViewById(R.id.btn_back_report).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportListener != null) pesananRef.removeEventListener(reportListener);
    }
}