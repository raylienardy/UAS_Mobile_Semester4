package com.example.foodcourtgo.users.menu;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.adapter.CartItemAdapter;
import com.example.foodcourtgo.model.CartHolder;
import com.example.foodcourtgo.model.CartItem;

public class CartActivity extends AppCompatActivity implements CartItemAdapter.OnCartActionListener {

    private RecyclerView rvCartItems;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartItemAdapter adapter;

    private String tenantId;
    private String tenantNama;
    private String mejaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_cart);

        tenantId = getIntent().getStringExtra("tenantId");
        tenantNama = getIntent().getStringExtra("tenantNama");
        mejaId = getIntent().getStringExtra("mejaId");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartItemAdapter(CartHolder.getCartList(), this);
        rvCartItems.setAdapter(adapter);

        updateTotal();

        btnCheckout.setOnClickListener(v -> {
            if (CartHolder.getCartList().isEmpty()) {
                Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            String tenantId = getIntent().getStringExtra("tenantId");
            if (tenantId == null) {
                Toast.makeText(this, "Error: tenant tidak diketahui", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
            intent.putExtra("tenantId", tenantId);
            intent.putExtra("tenantNama", tenantNama);
            intent.putExtra("mejaId", mejaId);
            startActivity(intent);
        });
    }

    private void updateTotal() {
        long total = CartHolder.getTotalHarga();
        tvTotal.setText("Rp" + String.format("%,d", total).replace(',', '.'));
    }

    @Override
    public void onQtyChanged(int position, int newQty) {
        CartItem item = CartHolder.getCartList().get(position);
        item.setQty(newQty);
        adapter.notifyItemChanged(position);
        updateTotal();
    }

    @Override
    public void onEditCatatan(int position, String currentCatatan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Catatan Makanan");
        EditText input = new EditText(this);
        input.setText(currentCatatan != null ? currentCatatan : "");
        builder.setView(input);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String catatanBaru = input.getText().toString();
            CartItem item = CartHolder.getCartList().get(position);
            item.setCatatan(catatanBaru);
            adapter.notifyItemChanged(position);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    @Override
    public void onDelete(int position) {
        CartHolder.removeItem(position);
        adapter.notifyItemRemoved(position);
        updateTotal();
        if (CartHolder.getCartList().isEmpty()) {
            finish(); // tutup activity jika kosong
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        updateTotal();
    }
}