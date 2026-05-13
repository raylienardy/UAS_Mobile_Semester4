package com.example.foodcourtgo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.example.foodcourtgo.users.menu.StatusPesananActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ActiveOrderAdapter extends RecyclerView.Adapter<ActiveOrderAdapter.ViewHolder> {

    private Context context;
    private List<PesananAdminModel> orderList;

    public ActiveOrderAdapter(Context context, List<PesananAdminModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_active_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PesananAdminModel order = orderList.get(position);
        holder.tvTenantName.setText(order.getTenantNama() != null ? order.getTenantNama() : "Tenant");
        holder.tvOrderId.setText("#" + order.getId());
        holder.tvTotal.setText(formatRupiah(order.getTotalHarga()));

        String status = order.getStatus();
        if (status.equals("pending")) {
            holder.tvStatus.setText("Menunggu konfirmasi");
            holder.tvStatus.setTextColor(context.getColor(R.color.blue_700));
        } else if (status.equals("processing")) {
            holder.tvStatus.setText("Sedang diproses");
            holder.tvStatus.setTextColor(context.getColor(R.color.orange_700));
        } else {
            holder.tvStatus.setText(status);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatusPesananActivity.class);
            intent.putExtra("pesananId", order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenantName, tvOrderId, tvStatus, tvTotal;
        ViewHolder(View itemView) {
            super(itemView);
            tvTenantName = itemView.findViewById(R.id.tvActiveTenantName);
            tvOrderId = itemView.findViewById(R.id.tvActiveOrderId);
            tvStatus = itemView.findViewById(R.id.tvActiveOrderStatus);
            tvTotal = itemView.findViewById(R.id.tvActiveOrderTotal);
        }
    }

    private String formatRupiah(long nominal) {
        return "Rp" + String.format("%,d", nominal).replace(',', '.');
    }
}