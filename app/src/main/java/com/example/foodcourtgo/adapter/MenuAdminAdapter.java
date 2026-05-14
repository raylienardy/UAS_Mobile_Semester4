package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.MenuModel;

import java.util.List;

public class MenuAdminAdapter extends RecyclerView.Adapter<MenuAdminAdapter.ViewHolder> {

    private List<MenuModel> menuList;
    private OnMenuActionListener listener;

    public interface OnMenuActionListener {
        void onEdit(MenuModel menu);
        void onDelete(MenuModel menu);
    }

    public MenuAdminAdapter(List<MenuModel> menuList, OnMenuActionListener listener) {
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_menu_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuModel menu = menuList.get(position);
        holder.tvName.setText(menu.getNama());
        holder.tvPrice.setText("Rp " + String.format("%,d", menu.getHarga()));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(menu);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(menu);
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_menu_name);
            tvPrice = itemView.findViewById(R.id.tv_menu_price);
            btnEdit = itemView.findViewById(R.id.btn_edit_menu);
            btnDelete = itemView.findViewById(R.id.btn_delete_menu);
        }
    }
}