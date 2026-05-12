package com.example.foodcourtgo.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.TenantModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    private Context context;
    private List<TenantModel> tenantList;
    private String userId; // ID user yang login, untuk menyimpan favorite

    public interface OnTenantClickListener {
        void onTenantClick(TenantModel tenant);
    }

    private OnTenantClickListener clickListener;

    // Constructor baru: tambahkan userId
    public TenantAdapter(Context context, List<TenantModel> tenantList,
                         OnTenantClickListener clickListener, String userId) {
        this.context = context;
        this.tenantList = tenantList;
        this.clickListener = clickListener;
        this.userId = userId;
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        TenantModel tenant = tenantList.get(position);
        holder.tvNama.setText(tenant.getNama());
        holder.tvKategori.setText(tenant.getKategori());
        holder.tvDeskripsi.setText(tenant.getDeskripsi());

        // Cek apakah tenant ini sudah difavoritkan oleh user
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("favorites")
                .child(userId).child(tenant.getId());
        favRef.get().addOnSuccessListener(snapshot -> {
            boolean isFav = snapshot.exists();
            holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        });

        // Klik tombol favorite
        holder.btnFavorite.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("favorites")
                    .child(userId).child(tenant.getId());
            ref.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // Sudah favorit, hapus
                    ref.removeValue().addOnSuccessListener(aVoid -> {
                        holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(context, "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Belum favorit, tambah
                    ref.setValue(true).addOnSuccessListener(aVoid -> {
                        holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                        Toast.makeText(context, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onTenantClick(tenant);
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    public static class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKategori, tvDeskripsi;
        ImageButton btnFavorite;

        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvTenantNama);
            tvKategori = itemView.findViewById(R.id.tvTenantKategori);
            tvDeskripsi = itemView.findViewById(R.id.tvTenantDeskripsi);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}