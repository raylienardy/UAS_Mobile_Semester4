package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.AkunModel;
import java.util.ArrayList;
import java.util.List;

public class AkunAdminAdapter extends RecyclerView.Adapter<AkunAdminAdapter.ViewHolder> {

    private List<AkunModel> akunList = new ArrayList<>();
    private OnAkunActionListener listener;

    public interface OnAkunActionListener {
        void onItemClick(AkunModel akun);
        void onLongClick(AkunModel akun); // untuk reset password / toggle aktif
    }

    public AkunAdminAdapter(OnAkunActionListener listener) {
        this.listener = listener;
    }

    public void setAkunList(List<AkunModel> list) {
        this.akunList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_akun, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AkunModel akun = akunList.get(position);
        holder.tvName.setText(akun.getName());
        holder.tvUsername.setText(akun.getUsername());
        holder.tvRole.setText(akun.getRole());
        holder.tvEmail.setText(akun.getEmail());
        holder.tvStatus.setText(akun.isActive() ? "Aktif" : "Nonaktif");
        holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(
                akun.isActive() ? R.color.green_700 : R.color.red_500));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(akun);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(akun);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return akunList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUsername, tvRole, tvEmail, tvStatus;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_akun_name);
            tvUsername = itemView.findViewById(R.id.tv_akun_username);
            tvRole = itemView.findViewById(R.id.tv_akun_role);
            tvEmail = itemView.findViewById(R.id.tv_akun_email);
            tvStatus = itemView.findViewById(R.id.tv_akun_status);
        }
    }
}