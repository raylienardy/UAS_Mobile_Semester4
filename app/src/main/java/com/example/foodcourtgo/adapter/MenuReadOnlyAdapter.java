package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.MenuModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuReadOnlyAdapter extends RecyclerView.Adapter<MenuReadOnlyAdapter.ViewHolder> {

    private List<MenuModel> menuList = new ArrayList<>();

    public void setMenuList(List<MenuModel> list) {
        this.menuList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_readonly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuModel menu = menuList.get(position);
        holder.tvNama.setText(menu.getNama());
        holder.tvDeskripsi.setText(menu.getDeskripsi());
        NumberFormat format = NumberFormat.getInstance(new Locale("id", "ID"));
        holder.tvHarga.setText("Rp " + format.format(menu.getHarga()));
        holder.tvTenantId.setText("Tenant: " + menu.getTenantId());
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDeskripsi, tvHarga, tvTenantId;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_menu_readonly_nama);
            tvDeskripsi = itemView.findViewById(R.id.tv_menu_readonly_deskripsi);
            tvHarga = itemView.findViewById(R.id.tv_menu_readonly_harga);
            tvTenantId = itemView.findViewById(R.id.tv_menu_readonly_tenant);
        }
    }
}