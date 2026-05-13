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

public class MenuAdminReadOnlyAdapter extends RecyclerView.Adapter<MenuAdminReadOnlyAdapter.ViewHolder> {

    private List<MenuModel> menuList = new ArrayList<>();

    public void setMenuList(List<MenuModel> list) {
        this.menuList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_readonly_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuModel menu = menuList.get(position);
        holder.tvNama.setText(menu.getNama());
        holder.tvDeskripsi.setText(menu.getDeskripsi());
        NumberFormat format = NumberFormat.getInstance(new Locale("id", "ID"));
        String harga = "Rp " + format.format(menu.getHarga());
        holder.tvHarga.setText(harga);
        holder.tvTenantId.setText("Tenant: " + menu.getTenantId()); // opsional
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDeskripsi, tvHarga, tvTenantId;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_menu_name);
            tvDeskripsi = itemView.findViewById(R.id.tv_menu_desc);
            tvHarga = itemView.findViewById(R.id.tv_menu_price);
            tvTenantId = itemView.findViewById(R.id.tv_menu_tenant);
        }
    }
}