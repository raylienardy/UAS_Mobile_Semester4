package com.example.foodcourtgo.admin.TenantManagement.addson_TenantManagementActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.addson.TenantModel;

import java.util.List;

public class TenantAdminAdapter extends RecyclerView.Adapter<TenantAdminAdapter.ViewHolder> {

    private List<TenantModel> tenantList;
    private OnTenantToggleListener toggleListener;

    public interface OnTenantToggleListener {
        void onToggle(TenantModel tenant);
    }

    public TenantAdminAdapter(List<TenantModel> tenantList, OnTenantToggleListener listener) {
        this.tenantList = tenantList;
        this.toggleListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TenantModel tenant = tenantList.get(position);

        holder.tvName.setText(tenant.getNama());
        holder.tvCategory.setText("Kategori: " + tenant.getKategori());

        boolean isActive = "active".equals(tenant.getStatus());
        holder.tvStatus.setText(isActive ? "Aktif" : "Nonaktif");
        holder.tvStatus.setBackgroundResource(isActive ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);

        holder.tvToggle.setText(isActive ? "Nonaktifkan" : "Aktifkan");
        holder.tvToggle.setOnClickListener(v -> {
            if (toggleListener != null) toggleListener.onToggle(tenant);
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvStatus, tvToggle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_tenant_name);
            tvCategory = itemView.findViewById(R.id.tv_tenant_category);
            tvStatus = itemView.findViewById(R.id.chip_tenant_status);
            tvToggle = itemView.findViewById(R.id.btn_toggle_tenant);
        }
    }
}