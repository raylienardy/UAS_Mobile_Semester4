package com.example.foodcourtgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodcourtgo.users.menu.addson_DetailTenantActivity.MenuModel;

import java.util.List;

public class MenuAdminAdapter extends RecyclerView.Adapter<MenuAdminAdapter.ViewHolder> {

    private List<MenuModel> menuList;
    private OnMenuActionListener listener;

    public interface OnMenuActionListener {
        void onDelete(MenuModel menu);
        void onEdit(MenuModel menu);
    }

    public MenuAdminAdapter(List<MenuModel> menuList, OnMenuActionListener listener) {
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuModel menu = menuList.get(position);
        holder.tvNama.setText(menu.getNama());
        holder.tvTenant.setText("Kategori: " + (menu.getKategori() != null ? menu.getKategori() : ""));
        holder.tvHarga.setText("Rp " + String.format("%,d", menu.getHarga()).replace(',', '.'));

        Glide.with(holder.itemView.getContext())
                .load(menu.getGambar())
                .placeholder(R.drawable.ic_menu_placeholder)
                .into(holder.ivGambar);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(menu);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(menu);
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGambar;
        TextView tvNama, tvTenant, tvHarga, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGambar = itemView.findViewById(R.id.iv_menu_image);
            tvNama = itemView.findViewById(R.id.tv_menu_name);
            tvTenant = itemView.findViewById(R.id.tv_menu_tenant);
            tvHarga = itemView.findViewById(R.id.tv_menu_price);
            btnDelete = itemView.findViewById(R.id.btn_delete_menu);
        }
    }
}