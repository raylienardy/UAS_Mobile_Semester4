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
        void onToggleStatus(TenantModel tenant);
        void onAssignResign(TenantModel tenant); // Assign jika belum ada owner, Resign jika sudah
        void onEditLokasi(TenantModel tenant);
        void onMoveOwner(TenantModel tenant);
        void onDeleteTenant(TenantModel tenant);
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
        holder.tvKategori.setText("Kategori: " + (tenant.getKategori() != null ? tenant.getKategori() : "-"));
        holder.tvStatus.setText(tenant.getStatus());

        // Tampilkan nama pemilik
        if (tenant.getOwnerName() != null && !tenant.getOwnerName().isEmpty()) {
            holder.tvOwner.setText("Pemilik: " + tenant.getOwnerName());
        } else {
            holder.tvOwner.setText("Pemilik: -");
        }

        boolean hasOwner = tenant.getOwnerId() != null && !tenant.getOwnerId().isEmpty();

        // Atur tombol Assign/Resign
        if (hasOwner) {
            holder.btnAssignResign.setText("Resign");
            holder.btnAssignResign.setBackgroundResource(R.drawable.bg_danger_button);
            holder.btnAssignResign.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        } else {
            holder.btnAssignResign.setText("Assign");
            holder.btnAssignResign.setBackgroundResource(R.drawable.button_biru);
            holder.btnAssignResign.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        }

        // Tampilkan/sembunyikan tombol Pindah Pemilik
        if (hasOwner) {
            holder.btnMoveOwner.setVisibility(View.VISIBLE);
        } else {
            holder.btnMoveOwner.setVisibility(View.GONE);
        }

        // Listener tombol
        holder.btnToggleStatus.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(tenant);
        });
        holder.btnAssignResign.setOnClickListener(v -> {
            if (listener != null) listener.onAssignResign(tenant);
        });
        holder.btnMoveLocation.setOnClickListener(v -> {
            if (listener != null) listener.onEditLokasi(tenant);
        });
        holder.btnMoveOwner.setOnClickListener(v -> {
            if (listener != null) listener.onMoveOwner(tenant);
        });
        holder.btnDeleteTenant.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteTenant(tenant);
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKategori, tvStatus, tvOwner;
        Button btnToggleStatus, btnAssignResign, btnMoveLocation, btnMoveOwner, btnDeleteTenant;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_tenant_nama);
            tvKategori = itemView.findViewById(R.id.tv_tenant_kategori);
            tvStatus = itemView.findViewById(R.id.tv_tenant_status);
            tvOwner = itemView.findViewById(R.id.tv_tenant_owner);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
            btnAssignResign = itemView.findViewById(R.id.btn_assign_resign);
            btnMoveLocation = itemView.findViewById(R.id.btn_move_location);
            btnMoveOwner = itemView.findViewById(R.id.btn_move_owner);
            btnDeleteTenant = itemView.findViewById(R.id.btn_delete_tenant);
        }
    }
}