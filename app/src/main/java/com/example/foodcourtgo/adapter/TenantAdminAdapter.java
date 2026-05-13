package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.TenantModel;
import java.util.ArrayList;
import java.util.List;

public class TenantAdminAdapter extends RecyclerView.Adapter<TenantAdminAdapter.ViewHolder> {

    private List<TenantModel> tenantList = new ArrayList<>();
    private OnTenantActionListener listener;

    public interface OnTenantActionListener {
        void onToggleStatus(TenantModel tenant);   // toggle aktif/nonaktif
        void onAssignAkun(TenantModel tenant);     // assign akun ke tenant
        void onEditLokasi(TenantModel tenant);
    }

    public TenantAdminAdapter(OnTenantActionListener listener) {
        this.listener = listener;
    }

    public void setTenantList(List<TenantModel> list) {
        this.tenantList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_tenant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TenantModel tenant = tenantList.get(position);
        holder.tvNama.setText(tenant.getNama());
        holder.tvKategori.setText("Kategori: " + tenant.getKategori());
        holder.tvStatus.setText(tenant.getStatus());

        // Warna status
        if ("active".equals(tenant.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.green_700));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.red_500));
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onEditLokasi(tenant);
            return true;
        });

        // Tombol toggle status
        holder.btnToggleStatus.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(tenant);
        });

        // Tombol assign akun
        holder.btnAssignAkun.setOnClickListener(v -> {
            if (listener != null) listener.onAssignAkun(tenant);
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKategori, tvStatus;
        Button btnToggleStatus, btnAssignAkun;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_tenant_nama);
            tvKategori = itemView.findViewById(R.id.tv_tenant_kategori);
            tvStatus = itemView.findViewById(R.id.tv_tenant_status);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
            btnAssignAkun = itemView.findViewById(R.id.btn_assign_akun);
        }
    }
}