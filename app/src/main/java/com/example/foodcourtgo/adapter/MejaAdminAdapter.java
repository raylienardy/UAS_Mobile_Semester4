package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.MejaModel;
import java.util.ArrayList;
import java.util.List;

public class MejaAdminAdapter extends RecyclerView.Adapter<MejaAdminAdapter.ViewHolder> {

    private List<MejaModel> mejaList = new ArrayList<>();
    private OnMejaActionListener listener;

    public interface OnMejaActionListener {
        void onItemClick(MejaModel meja);
        void onLongClick(MejaModel meja); // untuk opsi edit/hapus/qr
    }

    public MejaAdminAdapter(OnMejaActionListener listener) {
        this.listener = listener;
    }

    public void setMejaList(List<MejaModel> list) {
        this.mejaList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_meja, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MejaModel meja = mejaList.get(position);
        holder.tvNomor.setText("Meja " + meja.getNomor());
        holder.tvLokasi.setText(meja.getLokasi());
        holder.tvStatus.setText(meja.getStatus());
        // ubah warna status
        if ("available".equals(meja.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.green_700));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.red_500));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(meja);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(meja);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mejaList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomor, tvLokasi, tvStatus;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomor = itemView.findViewById(R.id.tv_meja_nomor);
            tvLokasi = itemView.findViewById(R.id.tv_meja_lokasi);
            tvStatus = itemView.findViewById(R.id.tv_meja_status);
        }
    }
}