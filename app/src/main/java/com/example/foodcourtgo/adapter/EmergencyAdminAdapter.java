package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.EmergencyModel;
import java.util.ArrayList;
import java.util.List;

public class EmergencyAdminAdapter extends RecyclerView.Adapter<EmergencyAdminAdapter.ViewHolder> {

    private List<EmergencyModel> list = new ArrayList<>();
    private OnEmergencyActionListener listener;

    public interface OnEmergencyActionListener {
        void onResolveClick(EmergencyModel emergency);
    }

    public EmergencyAdminAdapter(OnEmergencyActionListener listener) {
        this.listener = listener;
    }

    public void setList(List<EmergencyModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyModel em = list.get(position);
        holder.tvTitle.setText(em.getUserName() + " (" + em.getUserRole() + ")");
        holder.tvDetail.setText(em.getPesan());
        holder.tvTime.setText(em.getTimestamp());
        if (em.getMejaId() != null && !em.getMejaId().isEmpty()) {
            holder.tvLocation.setText("Meja: " + em.getMejaId());
        } else if (em.getTenantId() != null && !em.getTenantId().isEmpty()) {
            holder.tvLocation.setText("Tenant: " + em.getTenantId());
        } else {
            holder.tvLocation.setText("Lokasi: -");
        }
        holder.btnResolve.setOnClickListener(v -> {
            if (listener != null) listener.onResolveClick(em);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetail, tvTime, tvLocation;
        TextView btnResolve;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_emergency_title);
            tvDetail = itemView.findViewById(R.id.tv_emergency_detail);
            tvTime = itemView.findViewById(R.id.tv_emergency_time);
            tvLocation = itemView.findViewById(R.id.tv_emergency_location);
            btnResolve = itemView.findViewById(R.id.btn_resolve_emergency);
        }
    }
}