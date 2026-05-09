package com.example.foodcourtgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.users.menu.addson_PaymentActivity.PesananAdminModel;

import java.util.List;

public class TenantOrderAdapter extends RecyclerView.Adapter<TenantOrderAdapter.ViewHolder> {
    private List<PesananAdminModel> orderList;
    private OnOrderClickListener listener;
    public interface OnOrderClickListener { void onOrderClick(PesananAdminModel order); }

    public TenantOrderAdapter(List<PesananAdminModel> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }
    public void updateList(List<PesananAdminModel> newList) { this.orderList = newList; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tenant_order, parent, false);
        return new ViewHolder(v);
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PesananAdminModel order = orderList.get(position);
        holder.tvId.setText(order.getId());
        holder.tvStatus.setText(order.getStatus());
        holder.tvMeja.setText("Meja: " + order.getMeja());
        holder.tvTotal.setText("Rp " + String.format("%,d", order.getTotalHarga()));
        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }
    @Override public int getItemCount() { return orderList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvStatus, tvMeja, tvTotal;
        ViewHolder(View v) {
            super(v);
            tvId = v.findViewById(R.id.tv_order_id);
            tvStatus = v.findViewById(R.id.chip_order_status);
            tvMeja = v.findViewById(R.id.tv_order_table);
            tvTotal = v.findViewById(R.id.tv_order_total);
        }
    }
}