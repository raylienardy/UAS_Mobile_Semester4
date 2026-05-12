package com.example.foodcourtgo.tenant.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.addson.PesananAdminModel;

import java.util.List;

public class RecentOrderAdapter extends RecyclerView.Adapter<RecentOrderAdapter.ViewHolder> {
    private List<PesananAdminModel> orderList;

    public RecentOrderAdapter(List<PesananAdminModel> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PesananAdminModel order = orderList.get(position);
        holder.tvId.setText(order.getId());
        holder.tvTable.setText("Meja " + order.getMeja());
        holder.tvStatus.setText(order.getStatus());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<PesananAdminModel> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvTable, tvStatus;
        ViewHolder(View v) {
            super(v);
            tvId = v.findViewById(R.id.tv_recent_order_id);
            tvTable = v.findViewById(R.id.tv_recent_order_table);
            tvStatus = v.findViewById(R.id.tv_recent_order_status);
        }
    }
}