package com.example.foodcourtgo.users.addson_HomeActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;

import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    private Context context;
    private List<TenantModel> tenantList;

    // Interface klik item (buka detail)
    public interface OnTenantClickListener {
        void onTenantClick(TenantModel tenant);
    }

    private OnTenantClickListener clickListener;

    // Constructor baru: hanya satu listener
    public TenantAdapter(Context context, List<TenantModel> tenantList,
                         OnTenantClickListener clickListener) {
        this.context = context;
        this.tenantList = tenantList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        TenantModel tenant = tenantList.get(position);
        holder.tvNama.setText(tenant.getNama());
        holder.tvKategori.setText(tenant.getKategori());
        holder.tvDeskripsi.setText(tenant.getDeskripsi());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onTenantClick(tenant);
        });

        // Tidak ada lagi ivDelete, jadi tidak perlu di-bind
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    // Method untuk update list dari luar (opsional, jika kita set langsung lewat reference)
    // Tapi di HomeActivity kita menggunakan dua list berbeda, jadi cukup notify.
    // public void updateList(List<TenantModel> newList) { ... }

    public static class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKategori, tvDeskripsi;
        // ImageView ivDelete; // tidak diperlukan lagi

        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama     = itemView.findViewById(R.id.tvTenantNama);
            tvKategori = itemView.findViewById(R.id.tvTenantKategori);
            tvDeskripsi = itemView.findViewById(R.id.tvTenantDeskripsi);
            // ivDelete tidak ada
        }
    }
}