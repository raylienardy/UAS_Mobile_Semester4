package com.example.foodcourtgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.PesananAdminModel;

import java.util.List;

public class LaporanPesananAdapter extends RecyclerView.Adapter<LaporanPesananAdapter.ViewHolder> {
    private List<PesananAdminModel> list;

    public LaporanPesananAdapter(List<PesananAdminModel> list) {
        this.list = list;
    }

    public void updateList(List<PesananAdminModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_pesanan, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PesananAdminModel p = list.get(position);
        holder.tvId.setText(p.getId());
        holder.tvTanggal.setText(p.getWaktu());
        holder.tvTotal.setText("Rp " + String.format("%,d", p.getTotalHarga()));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvTanggal, tvTotal;
        ViewHolder(View v) {
            super(v);
            tvId = v.findViewById(R.id.tv_pesanan_id);
            tvTanggal = v.findViewById(R.id.tv_pesanan_tanggal);
            tvTotal = v.findViewById(R.id.tv_pesanan_total);
        }
    }
}