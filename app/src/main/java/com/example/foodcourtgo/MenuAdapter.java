package com.example.foodcourtgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private Context context;
    private List<MenuModel> menuList;
    private Map<String, Integer> pesananMap;

    public interface OnMenuClickListener {
        void onTambahClick(MenuModel menu, int position);
    }

    public interface OnPesananChangeListener {
        void onPesananChanged(int totalItems);
    }

    private OnMenuClickListener clickListener;
    private OnPesananChangeListener changeListener;

    public MenuAdapter(Context context, List<MenuModel> menuList,
                       Map<String, Integer> pesananMap,
                       OnMenuClickListener clickListener,
                       OnPesananChangeListener changeListener) {
        this.context = context;
        this.menuList = menuList;
        this.pesananMap = pesananMap;
        this.clickListener = clickListener;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuModel menu = menuList.get(position);
        String menuId = menu.getMenuId();

        holder.tvNama.setText(menu.getNama());
        holder.tvDeskripsi.setText(menu.getDeskripsi());
        holder.tvHarga.setText(menu.getHargaFormatted());

        Glide.with(context)
                .load(menu.getGambar())
                .placeholder(R.drawable.ic_menu_placeholder)
                .into(holder.ivGambar);

        // Quantity saat ini
        int qty = pesananMap.containsKey(menuId) ? pesananMap.get(menuId) : 0;

        // State tombol / kontrol
        if (holder.btnTambah != null && holder.llQuantityControl != null && holder.tvQuantity != null) {
            if (qty == 0) {
                holder.btnTambah.setVisibility(View.VISIBLE);
                holder.llQuantityControl.setVisibility(View.GONE);
            } else {
                holder.btnTambah.setVisibility(View.GONE);
                holder.llQuantityControl.setVisibility(View.VISIBLE);
                holder.tvQuantity.setText(String.valueOf(qty));
            }
        }

        // ✅ Tombol Tambah hanya buka detail opsi (TIDAK langsung menambah)
        holder.btnTambah.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onTambahClick(menu, position);
        });

        // ➖ Minus
        if (holder.btnMinus != null) {
            holder.btnMinus.setOnClickListener(v -> {
                int current = pesananMap.getOrDefault(menuId, 0);
                if (current > 1) {
                    pesananMap.put(menuId, current - 1);
                } else {
                    pesananMap.remove(menuId);
                }
                notifyItemChanged(position);
                updateBadge();
            });
        }

        // ➕ Plus
        if (holder.btnPlus != null) {
            holder.btnPlus.setOnClickListener(v -> {
                int current = pesananMap.getOrDefault(menuId, 0);
                pesananMap.put(menuId, current + 1);
                notifyItemChanged(position);
                updateBadge();
            });
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    private void updateBadge() {
        int total = 0;
        for (int q : pesananMap.values()) {
            total += q;
        }
        if (changeListener != null) {
            changeListener.onPesananChanged(total);   // ✅ hanya satu parameter
        }
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDeskripsi, tvHarga, btnTambah;
        ImageView ivGambar;
        TextView tvQuantity, btnMinus, btnPlus;
        View llQuantityControl;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvMenuNama);
            tvDeskripsi = itemView.findViewById(R.id.tvMenuDeskripsi);
            tvHarga = itemView.findViewById(R.id.tvMenuHarga);
            ivGambar = itemView.findViewById(R.id.ivMenuImage);
            btnTambah = itemView.findViewById(R.id.btnTambah);
            llQuantityControl = itemView.findViewById(R.id.llQuantityControl);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}