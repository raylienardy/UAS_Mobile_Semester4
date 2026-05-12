package com.example.foodcourtgo.users;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodcourtgo.R;

public class TakeAwayDineInActivity extends AppCompatActivity {

    private ImageView ivBack;
    private Button btnTakeAway, btnDineIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_takeaway_dinein);

        ivBack = findViewById(R.id.ivBack);
        btnTakeAway = findViewById(R.id.btnTakeAway);
        btnDineIn = findViewById(R.id.btnDineIn);

        ivBack.setOnClickListener(v -> finish());

        btnTakeAway.setOnClickListener(v -> {
            Intent intent = new Intent(TakeAwayDineInActivity.this, HomeActivity.class);
            intent.putExtra("orderMode", "TAKE_AWAY");
            startActivity(intent);
            finish();
        });

        btnDineIn.setOnClickListener(v -> {
            Intent intent = new Intent(TakeAwayDineInActivity.this, QrScannerActivity.class);
            startActivity(intent);
            finish();
        });
    }
}