package com.example.foodcourtgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TenantNotificationAdapter extends RecyclerView.Adapter<TenantNotificationAdapter.ViewHolder> {
    private List<NotificationModel> list;
    private OnNotificationClick listener;
    public interface OnNotificationClick { void onClick(NotificationModel notif); }

    public TenantNotificationAdapter(List<NotificationModel> list, OnNotificationClick listener) {
        this.list = list;
        this.listener = listener;
    }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tenant_notification, parent, false);
        return new ViewHolder(v);
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel n = list.get(position);
        holder.tvText.setText(n.getText());
        holder.tvTime.setText(n.getWaktu());
        holder.itemView.setOnClickListener(v -> listener.onClick(n));
    }
    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;
        ViewHolder(View v) {
            super(v);
            tvText = v.findViewById(R.id.tv_notification_text);
            tvTime = v.findViewById(R.id.tv_notification_time);
        }
    }
}