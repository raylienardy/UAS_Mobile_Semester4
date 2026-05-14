package com.example.foodcourtgo.admin.DashboardAdmin;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodcourtgo.R;
import com.example.foodcourtgo.model.ItemPesananModel;
import com.example.foodcourtgo.model.PesananAdminModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LaporanActivity extends AppCompatActivity {

    private TextView tvReportIncome, tvReportOrder;
    private LinearLayout chartContainer;
    private RecyclerView rvTopMenu;
    private DatabaseReference pesananRef;

    private String currentFilter = "daily";
    private int currentYear, currentMonth;
    private List<PesananAdminModel> allOrders = new ArrayList<>();
    private TopMenuAdapter topMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_laporan);

        tvReportIncome = findViewById(R.id.tv_report_income);
        tvReportOrder = findViewById(R.id.tv_report_order);
        chartContainer = findViewById(R.id.chart_container);
        rvTopMenu = findViewById(R.id.rv_top_menu);

        rvTopMenu.setLayoutManager(new LinearLayoutManager(this));
        topMenuAdapter = new TopMenuAdapter();
        rvTopMenu.setAdapter(topMenuAdapter);

        pesananRef = FirebaseDatabase.getInstance().getReference("pesanan");

        findViewById(R.id.filter_daily).setOnClickListener(v -> {
            currentFilter = "daily";
            highlightFilter(R.id.filter_daily);
            loadAndDisplayData();
        });
        findViewById(R.id.filter_monthly).setOnClickListener(v -> {
            currentFilter = "monthly";
            highlightFilter(R.id.filter_monthly);
            loadAndDisplayData();
        });
        findViewById(R.id.filter_yearly).setOnClickListener(v -> {
            currentFilter = "yearly";
            highlightFilter(R.id.filter_yearly);
            loadAndDisplayData();
        });

        findViewById(R.id.btn_export_report).setOnClickListener(v -> exportData());
        findViewById(R.id.btn_back_laporan).setOnClickListener(v -> finish());

        loadAllOrders();
    }

    private void highlightFilter(int filterId) {
        // Reset semua tombol ke tampilan inactive
        TextView daily = findViewById(R.id.filter_daily);
        TextView monthly = findViewById(R.id.filter_monthly);
        TextView yearly = findViewById(R.id.filter_yearly);

        daily.setBackgroundResource(R.drawable.bg_card);
        daily.setTextColor(getColor(R.color.dark_700));
        daily.setTypeface(null, Typeface.NORMAL);

        monthly.setBackgroundResource(R.drawable.bg_card);
        monthly.setTextColor(getColor(R.color.dark_700));
        monthly.setTypeface(null, Typeface.NORMAL);

        yearly.setBackgroundResource(R.drawable.bg_card);
        yearly.setTextColor(getColor(R.color.dark_700));
        yearly.setTypeface(null, Typeface.NORMAL);

        // Aktifkan tombol yang dipilih
        TextView activeView = findViewById(filterId);
        activeView.setBackgroundResource(R.drawable.bg_nav_active);
        activeView.setTextColor(getColor(R.color.blue_700));
        activeView.setTypeface(null, Typeface.BOLD);
    }

    private void loadAllOrders() {
        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PesananAdminModel order = snap.getValue(PesananAdminModel.class);
                    if (order != null) {
                        order.setId(snap.getKey());
                        allOrders.add(order);
                    }
                }
                loadAndDisplayData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LaporanActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAndDisplayData() {
        List<PesananAdminModel> filtered = filterOrdersByPeriod(allOrders, currentFilter);
        long totalIncome = 0;
        for (PesananAdminModel order : filtered) {
            totalIncome += order.getTotalHarga();
        }
        int totalOrders = filtered.size();

        tvReportOrder.setText(String.valueOf(totalOrders));
        // Format pendapatan seperti di Dashboard
        tvReportIncome.setText("Rp " + String.format("%,d", totalIncome).replace(',', '.'));

        displayChart(filtered, currentFilter);
        calculateTopMenus(filtered);
    }

    private List<PesananAdminModel> filterOrdersByPeriod(List<PesananAdminModel> orders, String period) {
        Calendar now = Calendar.getInstance();
        currentYear = now.get(Calendar.YEAR);
        currentMonth = now.get(Calendar.MONTH);
        List<PesananAdminModel> result = new ArrayList<>();

        for (PesananAdminModel order : orders) {
            String waktuStr = order.getWaktu();
            if (waktuStr == null || waktuStr.isEmpty()) continue;

            // Coba ekstrak jam (HH.mm) untuk daily
            if (period.equals("daily")) {
                // Karena tidak ada tanggal, asumsikan semua pesanan terjadi hari ini
                // Tapi kita tetap butuh jam untuk grafik. Jadi semua pesanan masuk daily.
                result.add(order);
            } else if (period.equals("monthly")) {
                // Tidak bisa filter bulan, tampilkan semua (atau kosongkan)
                // Kita pilih tampilkan semua agar ada data
                result.add(order);
            } else if (period.equals("yearly")) {
                result.add(order);
            }
        }
        return result;
    }

    private void displayChart(List<PesananAdminModel> filtered, String period) {
        chartContainer.removeAllViews();
        if (filtered.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Tidak ada data untuk periode ini");
            emptyText.setGravity(android.view.Gravity.CENTER);
            chartContainer.addView(emptyText);
            return;
        }

        Map<String, Long> dataMap = new HashMap<>();
        long maxValue = 0;

        if (period.equals("daily")) {
            // Inisialisasi 24 jam
            for (int i = 0; i < 24; i++) {
                dataMap.put(String.format("%02d:00", i), 0L);
            }
            for (PesananAdminModel order : filtered) {
                String waktuStr = order.getWaktu();
                if (waktuStr != null && waktuStr.contains(".")) {
                    String hour = waktuStr.split("\\.")[0];
                    if (hour.length() == 1) hour = "0" + hour;
                    String key = hour + ":00";
                    long newValue = dataMap.getOrDefault(key, 0L) + order.getTotalHarga();
                    dataMap.put(key, newValue);
                    if (newValue > maxValue) maxValue = newValue;
                }
            }
        } else if (period.equals("monthly")) {
            // Karena tidak ada tanggal, kita agregasi semua ke satu bar (misal "Total")
            long total = 0;
            for (PesananAdminModel order : filtered) total += order.getTotalHarga();
            dataMap.put("Total", total);
            maxValue = total;
        } else { // yearly
            long total = 0;
            for (PesananAdminModel order : filtered) total += order.getTotalHarga();
            dataMap.put("Total", total);
            maxValue = total;
        }

        if (maxValue == 0) maxValue = 1;
        int maxHeight = 150;
        int barMaxHeightPx = (int) (maxHeight * getResources().getDisplayMetrics().density);

        for (Map.Entry<String, Long> entry : dataMap.entrySet()) {
            int barHeightPx = (int) ((entry.getValue() * barMaxHeightPx) / maxValue);
            if (barHeightPx < 8 && entry.getValue() > 0) barHeightPx = 8;
            addBarToChart(entry.getKey(), barHeightPx);
        }
    }

    private void addBarToChart(String label, int barHeightPx) {
        LinearLayout barLayout = new LinearLayout(this);
        barLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        barLayout.setLayoutParams(layoutParams);
        barLayout.setPadding(4, 0, 4, 0);
        barLayout.setGravity(android.view.Gravity.BOTTOM);

        View bar = new View(this);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, barHeightPx);
        bar.setLayoutParams(barParams);
        bar.setBackgroundResource(R.drawable.bg_chart_bar);
        barLayout.addView(bar);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(10);
        labelView.setGravity(android.view.Gravity.CENTER);
        labelView.setPadding(0, 4, 0, 0);
        barLayout.addView(labelView);

        chartContainer.addView(barLayout);
    }

    private void calculateTopMenus(List<PesananAdminModel> orders) {
        Map<String, Integer> menuCountMap = new HashMap<>();
        for (PesananAdminModel order : orders) {
            if (order.getItems() != null) {
                for (ItemPesananModel item : order.getItems()) {
                    String menuName = item.getNama();
                    int qty = item.getQty();
                    menuCountMap.put(menuName, menuCountMap.getOrDefault(menuName, 0) + qty);
                }
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(menuCountMap.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<TopMenuModel> topMenus = new ArrayList<>();
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            topMenus.add(new TopMenuModel(list.get(i).getKey(), list.get(i).getValue()));
        }
        topMenuAdapter.setTopMenus(topMenus);
    }

    private void exportData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Laporan FoodCourt Go\n");
        sb.append("Periode: ").append(currentFilter).append("\n");
        sb.append("Total Pesanan: ").append(tvReportOrder.getText()).append("\n");
        sb.append("Total Pendapatan: ").append(tvReportIncome.getText()).append("\n\n");
        sb.append("Menu Terlaris:\n");
        for (TopMenuModel menu : topMenuAdapter.getTopMenus()) {
            sb.append("- ").append(menu.name).append(": ").append(menu.count).append(" pesanan\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Ekspor Data")
                .setMessage("Data siap diekspor. (Fungsi ekspor file dapat ditambahkan kemudian)")
                .setPositiveButton("OK", null)
                .show();
        Toast.makeText(this, "Simulasi ekspor data", Toast.LENGTH_SHORT).show();
    }

    // --- Model dan Adapter (sama seperti sebelumnya) ---
    static class TopMenuModel {
        String name;
        int count;
        TopMenuModel(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    class TopMenuAdapter extends RecyclerView.Adapter<TopMenuAdapter.ViewHolder> {
        private List<TopMenuModel> list = new ArrayList<>();

        public void setTopMenus(List<TopMenuModel> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        public List<TopMenuModel> getTopMenus() { return list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_top_menu, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TopMenuModel item = list.get(position);
            holder.tvMenuName.setText((position+1) + ". " + item.name);
            holder.tvMenuCount.setText(item.count + " pesanan");
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMenuName, tvMenuCount;
            ViewHolder(View itemView) {
                super(itemView);
                tvMenuName = itemView.findViewById(R.id.tv_top_menu_name);
                tvMenuCount = itemView.findViewById(R.id.tv_top_menu_count);
            }
        }
    }
}