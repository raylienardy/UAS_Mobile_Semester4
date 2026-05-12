package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.CartItem;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private List<CartItem> cartList;
    private OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQtyChanged(int position, int newQty);
        void onEditCatatan(int position, String currentCatatan);
        void onDelete(int position);
    }

    public CartItemAdapter(List<CartItem> cartList, OnCartActionListener listener) {
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.tvNama.setText(item.getNama());
        holder.tvHarga.setText("Rp" + String.format("%,d", item.getTotalHarga()).replace(',', '.'));
        holder.tvQty.setText(String.valueOf(item.getQty()));

        if (item.getOpsi() != null && !item.getOpsi().isEmpty()) {
            holder.tvOpsi.setVisibility(View.VISIBLE);
            holder.tvOpsi.setText(item.getOpsi());
        } else {
            holder.tvOpsi.setVisibility(View.GONE);
        }

        if (item.getCatatan() != null && !item.getCatatan().isEmpty()) {
            holder.tvCatatan.setVisibility(View.VISIBLE);
            holder.tvCatatan.setText("Catatan: " + item.getCatatan());
        } else {
            holder.tvCatatan.setVisibility(View.GONE);
        }

        holder.btnMinus.setOnClickListener(v -> {
            int newQty = item.getQty() - 1;
            if (newQty >= 1) {
                listener.onQtyChanged(position, newQty);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            listener.onQtyChanged(position, item.getQty() + 1);
        });

        holder.btnEditCatatan.setOnClickListener(v -> {
            listener.onEditCatatan(position, item.getCatatan());
        });

        holder.btnHapus.setOnClickListener(v -> {
            listener.onDelete(position);
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvOpsi, tvCatatan, tvHarga, tvQty;
        ImageButton btnMinus, btnPlus;
        Button btnEditCatatan, btnHapus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvOpsi = itemView.findViewById(R.id.tvOpsi);
            tvCatatan = itemView.findViewById(R.id.tvCatatan);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvQty = itemView.findViewById(R.id.tvQty);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnEditCatatan = itemView.findViewById(R.id.btnEditCatatan);
            btnHapus = itemView.findViewById(R.id.btnHapus);
        }
    }
}