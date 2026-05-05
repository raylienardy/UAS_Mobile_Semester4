package com.example.foodcourtgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TenantNotificationAdapter extends RecyclerView.Adapter<TenantNotificationAdapter.ViewHolder> {

    private List<NotificationModel> notificationList;

    public TenantNotificationAdapter(List<NotificationModel> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        holder.tvText.setText(notification.getText());
        holder.tvTime.setText(notification.getWaktu());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_notification_text);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
        }
    }
}