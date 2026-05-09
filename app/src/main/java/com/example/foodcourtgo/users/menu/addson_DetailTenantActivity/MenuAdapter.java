package com.example.foodcourtgo.users.menu.addson_DetailTenantActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodcourtgo.R;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private Context context;
    private List<MenuModel> menuList;
    private OnMenuClickListener clickListener;

    public interface OnMenuClickListener {
        void onTambahClick(MenuModel menu, int position);
    }

    public MenuAdapter(Context context, List<MenuModel> menuList, OnMenuClickListener clickListener) {
        this.context = context;
        this.menuList = menuList;
        this.clickListener = clickListener;
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
        holder.tvNama.setText(menu.getNama());
        holder.tvDeskripsi.setText(menu.getDeskripsi());
        holder.tvHarga.setText(menu.getHargaFormatted());

        Glide.with(context)
                .load(menu.getGambar())
                .placeholder(R.drawable.ic_menu_placeholder)
                .into(holder.ivGambar);

        holder.btnTambah.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onTambahClick(menu, position);
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDeskripsi, tvHarga, btnTambah;
        ImageView ivGambar;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvMenuNama);
            tvDeskripsi = itemView.findViewById(R.id.tvMenuDeskripsi);
            tvHarga = itemView.findViewById(R.id.tvMenuHarga);
            ivGambar = itemView.findViewById(R.id.ivMenuImage);
            btnTambah = itemView.findViewById(R.id.btnTambah);
        }
    }
}