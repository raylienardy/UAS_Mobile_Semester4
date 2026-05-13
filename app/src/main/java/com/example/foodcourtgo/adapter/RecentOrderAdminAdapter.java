package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.PesananAdminModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentOrderAdminAdapter extends RecyclerView.Adapter<RecentOrderAdminAdapter.ViewHolder> {

    private List<PesananAdminModel> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(PesananAdminModel order);
    }

    public RecentOrderAdminAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<PesananAdminModel> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_order_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PesananAdminModel order = orders.get(position);
        NumberFormat format = NumberFormat.getInstance(new Locale("id", "ID"));
        String total = "Rp " + format.format(order.getTotalHarga());

        holder.tvOrderId.setText(order.getId());
        holder.tvTenant.setText(order.getTenantNama());
        holder.tvTotal.setText(total);
        holder.tvStatus.setText(order.getStatus());

        // Warna status
        int color;
        switch (order.getStatus()) {
            case "pending":
                color = holder.itemView.getContext().getColor(R.color.yellow_700);
                break;
            case "processing":
                color = holder.itemView.getContext().getColor(R.color.blue_700);
                break;
            case "done":
                color = holder.itemView.getContext().getColor(R.color.green_700);
                break;
            default:
                color = holder.itemView.getContext().getColor(R.color.dark_700);
        }
        holder.tvStatus.setTextColor(color);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTenant, tvTotal, tvStatus;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_recent_order_id);
            tvTenant = itemView.findViewById(R.id.tv_recent_order_tenant);
            tvTotal = itemView.findViewById(R.id.tv_recent_order_total);
            tvStatus = itemView.findViewById(R.id.tv_recent_order_status);
        }
    }
}