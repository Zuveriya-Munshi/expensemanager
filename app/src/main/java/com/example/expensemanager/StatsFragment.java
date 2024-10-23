package com.example.expensemanager;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.expensemanager.Model.Data;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatsFragment extends Fragment {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;
    private String[] type = {"Income", "Expense"};
    private int[] values = {0, 0};
    private Map<Date, Integer> DateWiseIncome = new TreeMap<>();
    private Map<Date, Integer> DateWiseExpense = new TreeMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_stats, container, false);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);
        mIncomeDatabase.keepSynced(true);
        mExpenseDatabase.keepSynced(true);

        // Firebase Income Data Listener
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                values[0] = 0;
                DateWiseIncome.clear();
                for (DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    values[0] += data.getAmount();
                    DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = format.parse(data.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date != null) {
                        DateWiseIncome.put(date, DateWiseIncome.containsKey(date) ?
                                DateWiseIncome.get(date) + data.getAmount() : data.getAmount());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Firebase Expense Data Listener
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                values[1] = 0;
                DateWiseExpense.clear();
                for (DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    values[1] += data.getAmount();
                    DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = format.parse(data.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date != null) {
                        DateWiseExpense.put(date, DateWiseExpense.containsKey(date) ?
                                DateWiseExpense.get(date) + data.getAmount() : data.getAmount());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Pie Chart Setup
        setupPieChart(myView);

        // Line Chart for Income
        setupLineChart(myView, DateWiseIncome, R.id.linechart, "Income", 0xFF669900);

        // Line Chart for Expense
        setupLineChart(myView, DateWiseExpense, R.id.lineChart1, "Expense", 0xFFCC0000);

        return myView;
    }

    private void setupPieChart(View view) {
        PieChart pieChart = view.findViewById(R.id.piechart);
        ArrayList<PieEntry> dataEntries = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            dataEntries.add(new PieEntry(values[i], type[i]));
        }

        int[] colorClassArray = new int[]{0xFF669900, 0xFFCC0000};
        PieDataSet pieDataSet = new PieDataSet(dataEntries, "");
        pieDataSet.setColors(colorClassArray);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(25);
        pieChart.setData(pieData);
        pieChart.animateXY(2000, 2000);
        pieChart.setDrawHoleEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(18);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextColor(Color.CYAN);
        legend.setEnabled(true);

        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void setupLineChart(View view, Map<Date, Integer> dataMap, int chartId, String label, int color) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();
        String[] xAxisValues = new String[dataMap.size()];

        int index = 0;
        for (Map.Entry<Date, Integer> entry : dataMap.entrySet()) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            xAxisValues[index] = formatter.format(entry.getKey());
            entries.add(new Entry(index, entry.getValue()));
            index++;
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setColor(color);
        lineDataSet.setValueTextColor(Color.CYAN);
        lineDataSet.setValueTextSize(15);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setLineWidth(4f);
        lineDataSet.setCircleRadius(3f);

        dataSets.add(lineDataSet);

        LineChart lineChart = view.findViewById(chartId);
        lineChart.setData(new LineData(dataSets));
        lineChart.animateX(3000);

        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setTextColor(Color.BLUE);
        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setEnabled(true);
        leftYAxis.setTextColor(Color.BLUE);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLUE);
        xAxis.setLabelCount(dataMap.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }
}
