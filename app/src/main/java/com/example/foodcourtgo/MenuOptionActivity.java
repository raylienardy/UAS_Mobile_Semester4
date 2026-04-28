package com.example.foodcourtgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MenuOptionActivity extends AppCompatActivity {

    private ImageView ivBack, ivGambar;
    private TextView tvNama, tvDeskripsi, tvHarga;
    private CheckBox cbPedas, cbPorsiBesar;
    private Button btnPilih;

    private String menuId, menuNama, menuDeskripsi, menuGambar;
    private long menuHarga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_option);

        ivBack = findViewById(R.id.ivBack);
        ivGambar = findViewById(R.id.ivMenuImage);
        tvNama = findViewById(R.id.tvMenuNama);
        tvDeskripsi = findViewById(R.id.tvMenuDeskripsi);
        tvHarga = findViewById(R.id.tvMenuHarga);
        cbPedas = findViewById(R.id.cbPedas);
        cbPorsiBesar = findViewById(R.id.cbPorsiBesar);
        btnPilih = findViewById(R.id.btnPilih);

        Intent intent = getIntent();
        menuId = intent.getStringExtra("menuId");
        menuNama = intent.getStringExtra("menuNama");
        menuDeskripsi = intent.getStringExtra("menuDeskripsi");
        menuGambar = intent.getStringExtra("menuGambar");
        menuHarga = intent.getLongExtra("menuHarga", 0);

        tvNama.setText(menuNama);
        tvDeskripsi.setText(menuDeskripsi);
        tvHarga.setText("Rp" + String.format("%,d", menuHarga).replace(',', '.'));
        Glide.with(this).load(menuGambar).into(ivGambar);

        ivBack.setOnClickListener(v -> finish());

        btnPilih.setOnClickListener(v -> {
            long hargaTambahan = cbPorsiBesar.isChecked() ? 5000 : 0;
            boolean isPedas = cbPedas.isChecked();
            boolean isPorsiBesar = cbPorsiBesar.isChecked();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("menuId", menuId);
            resultIntent.putExtra("menuNama", menuNama);
            resultIntent.putExtra("hargaTambahan", hargaTambahan);
            resultIntent.putExtra("isPedas", isPedas);
            resultIntent.putExtra("isPorsiBesar", isPorsiBesar);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}