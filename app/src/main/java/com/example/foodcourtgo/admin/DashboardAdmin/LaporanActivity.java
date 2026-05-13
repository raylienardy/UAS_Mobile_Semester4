package com.example.foodcourtgo.admin.DashboardAdmin;

import android.app.AlertDialog;
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

import java.text.NumberFormat;
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
        // Gunakan warna yang pasti ada di project
        int defaultBg = getResources().getColor(R.color.white);
        int activeBg = getResources().getColor(R.color.blue_700);
        int defaultText = getResources().getColor(R.color.dark_700);
        int activeText = getResources().getColor(R.color.white);

        findViewById(R.id.filter_daily).setBackgroundColor(defaultBg);
        ((TextView) findViewById(R.id.filter_daily)).setTextColor(defaultText);
        findViewById(R.id.filter_monthly).setBackgroundColor(defaultBg);
        ((TextView) findViewById(R.id.filter_monthly)).setTextColor(defaultText);
        findViewById(R.id.filter_yearly).setBackgroundColor(defaultBg);
        ((TextView) findViewById(R.id.filter_yearly)).setTextColor(defaultText);

        TextView activeView = findViewById(filterId);
        activeView.setBackgroundColor(activeBg);
        activeView.setTextColor(activeText);
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

        NumberFormat format = NumberFormat.getInstance(new Locale("id", "ID"));
        tvReportOrder.setText(String.valueOf(totalOrders));
        tvReportIncome.setText("Rp " + format.format(totalIncome).replace(',', '.'));

        displayChart(filtered, currentFilter);
        calculateTopMenus(filtered);
    }

    private List<PesananAdminModel> filterOrdersByPeriod(List<PesananAdminModel> orders, String period) {
        Calendar now = Calendar.getInstance();
        currentYear = now.get(Calendar.YEAR);
        currentMonth = now.get(Calendar.MONTH);
        List<PesananAdminModel> result = new ArrayList<>();
        SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (PesananAdminModel order : orders) {
            String waktuStr = order.getWaktu();
            if (waktuStr == null || waktuStr.isEmpty()) continue;
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdfFull.parse(waktuStr));
                int tahun = cal.get(Calendar.YEAR);
                int bulan = cal.get(Calendar.MONTH);
                int hari = cal.get(Calendar.DAY_OF_YEAR);

                if (period.equals("daily")) {
                    if (tahun == currentYear && bulan == currentMonth && hari == now.get(Calendar.DAY_OF_YEAR)) {
                        result.add(order);
                    }
                } else if (period.equals("monthly")) {
                    if (tahun == currentYear && bulan == currentMonth) {
                        result.add(order);
                    }
                } else if (period.equals("yearly")) {
                    if (tahun == currentYear) {
                        result.add(order);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void displayChart(List<PesananAdminModel> filtered, String period) {
        chartContainer.removeAllViews();
        Map<String, Long> dataMap = new HashMap<>();

        if (period.equals("daily")) {
            for (int i = 0; i < 24; i++) {
                dataMap.put(String.format("%02d:00", i), 0L);
            }
            for (PesananAdminModel order : filtered) {
                String waktuStr = order.getWaktu();
                if (waktuStr != null && waktuStr.length() >= 13) {
                    String hour = waktuStr.substring(11, 13);
                    String key = hour + ":00";
                    dataMap.put(key, dataMap.getOrDefault(key, 0L) + order.getTotalHarga());
                }
            }
            for (int i = 0; i < 24; i++) {
                String key = String.format("%02d:00", i);
                addBarToChart(key, dataMap.get(key));
            }
        } else if (period.equals("monthly")) {
            Calendar cal = Calendar.getInstance();
            int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= maxDay; i++) {
                dataMap.put(String.valueOf(i), 0L);
            }
            for (PesananAdminModel order : filtered) {
                String waktuStr = order.getWaktu();
                if (waktuStr != null && waktuStr.length() >= 10) {
                    String dayStr = waktuStr.substring(8, 10);
                    int day = Integer.parseInt(dayStr);
                    String key = String.valueOf(day);
                    dataMap.put(key, dataMap.getOrDefault(key, 0L) + order.getTotalHarga());
                }
            }
            for (int i = 1; i <= maxDay; i++) {
                addBarToChart(String.valueOf(i), dataMap.get(String.valueOf(i)));
            }
        } else if (period.equals("yearly")) {
            for (int i = 1; i <= 12; i++) {
                dataMap.put(getMonthName(i), 0L);
            }
            for (PesananAdminModel order : filtered) {
                String waktuStr = order.getWaktu();
                if (waktuStr != null && waktuStr.length() >= 7) {
                    int month = Integer.parseInt(waktuStr.substring(5, 7));
                    String key = getMonthName(month);
                    dataMap.put(key, dataMap.getOrDefault(key, 0L) + order.getTotalHarga());
                }
            }
            for (int i = 1; i <= 12; i++) {
                addBarToChart(getMonthName(i), dataMap.get(getMonthName(i)));
            }
        }
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des"};
        return months[month - 1];
    }

    private void addBarToChart(String label, long value) {
        int maxHeight = 200;
        int barHeight = (int) (value / 1000); // skala sederhana
        if (barHeight > maxHeight) barHeight = maxHeight;
        if (barHeight < 20 && value > 0) barHeight = 20;

        LinearLayout barLayout = new LinearLayout(this);
        barLayout.setOrientation(LinearLayout.VERTICAL);
        barLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        barLayout.setPadding(4, 0, 4, 0);
        barLayout.setGravity(android.view.Gravity.BOTTOM);

        View bar = new View(this);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, barHeight);
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