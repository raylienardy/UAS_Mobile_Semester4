package com.example.foodcourtgo.admin.Pesanan.addson_PesananActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.addson_PaymentActivity_PesananActivity.ItemPesananModel;
import com.example.foodcourtgo.addson_PaymentActivity_PesananActivity.PesananAdminModel;

import java.util.List;

public class PesananAdminAdapter extends RecyclerView.Adapter<PesananAdminAdapter.ViewHolder> {

    private List<PesananAdminModel> pesananList;
    private OnPesananClickListener listener;

    public interface OnPesananClickListener {
        void onPesananClick(PesananAdminModel pesanan);
    }

    public PesananAdminAdapter(List<PesananAdminModel> pesananList, OnPesananClickListener listener) {
        this.pesananList = pesananList;
        this.listener = listener;
    }

    public void updateList(List<PesananAdminModel> newList) {
        this.pesananList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pesanan_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PesananAdminModel pesanan = pesananList.get(position);
        holder.tvOrderId.setText("#" + pesanan.getId());
        holder.tvTenant.setText(pesanan.getTenantNama());
        holder.tvTable.setText("Meja " + pesanan.getMeja() + " • " + pesanan.getWaktu());
        holder.tvStatus.setText(pesanan.getStatus());

        // Tampilkan item
        StringBuilder itemsStr = new StringBuilder();
        if (pesanan.getItems() != null) {
            for (ItemPesananModel item : pesanan.getItems()) {
                if (itemsStr.length() > 0) itemsStr.append(", ");
                itemsStr.append(item.getNama()).append(" x").append(item.getQty());
            }
        }
        holder.tvItems.setText(itemsStr.toString());
        holder.tvTotal.setText("Total: Rp " + String.format("%,d", pesanan.getTotalHarga()).replace(',', '.'));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPesananClick(pesanan);
        });
    }

    @Override
    public int getItemCount() {
        return pesananList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTenant, tvTable, tvStatus, tvItems, tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvTenant = itemView.findViewById(R.id.tv_order_tenant);
            tvTable = itemView.findViewById(R.id.tv_order_table);
            tvStatus = itemView.findViewById(R.id.chip_order_status);
            tvItems = itemView.findViewById(R.id.tv_order_items);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
        }
    }
}