package com.example.foodcourtgo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.users.menu.addson_PaymentActivity.NotificationModel;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class TenantNotificationsActivity extends AppCompatActivity {
    RecyclerView rv;
    TenantNotificationAdapter adapter;
    List<NotificationModel> list = new ArrayList<>();
    String tenantId;
    DatabaseReference notifRef;
    ValueEventListener notifListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_notifications);

        SharedPreferences pref = getSharedPreferences("FoodCourtGoPrefs", MODE_PRIVATE);
        tenantId = pref.getString("tenantId", "");

        rv = findViewById(R.id.rv_tenant_notifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TenantNotificationAdapter(list, notif -> {
            notifRef.child(notif.getId()).child("status").setValue("read");
            Toast.makeText(this, notif.getText(), Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        notifRef = FirebaseDatabase.getInstance().getReference("notifications");
        notifListener = notifRef.orderByChild("tenantId").equalTo(tenantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            NotificationModel n = ds.getValue(NotificationModel.class);
                            if (n != null) {
                                n.setId(ds.getKey());
                                list.add(n);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });

        findViewById(R.id.btn_back_notifications).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifListener != null) notifRef.removeEventListener(notifListener);
    }
}