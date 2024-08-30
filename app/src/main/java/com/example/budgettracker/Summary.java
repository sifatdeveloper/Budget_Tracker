package com.example.budgettracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Summary extends Fragment {

    private PieChart pieChart;
    private ExecutorService executorService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        setupPieChart();

        return view;
    }

    private void setupPieChart() {
        // Initialize ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // Fetch data asynchronously
        executorService.execute(() -> {
            List<PieEntry> entries = getCategoryData();

            // Update the pie chart on the UI thread
            getActivity().runOnUiThread(() -> {
                PieDataSet dataSet = new PieDataSet(entries, "Income and Expense Categories");
                dataSet.setColors(new int[]{
                        Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW, Color.CYAN,
                        Color.MAGENTA, Color.GRAY, Color.LTGRAY, Color.DKGRAY, Color.BLACK
                }); // Adjust colors as needed
                dataSet.setValueTextColor(Color.WHITE);
                dataSet.setValueTextSize(12f);

                PieData pieData = new PieData(dataSet);
                pieChart.setData(pieData);

                Description description = new Description();
                description.setText("Income and Expense Categories");
                pieChart.setDescription(description);

                pieChart.invalidate(); // Refresh the chart
            });
        });
    }

    private List<PieEntry> getCategoryData() {
        List<PieEntry> entries = new ArrayList<>();
        List<Expense> allExpenses = ExpenseDatabase.getInstance(getContext()).expenseDao().getAllExpenses();

        // Group expenses by category and type, then sum them up
        Map<String, Double> categoryTotals = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getType() + ": " + expense.getCategory(),
                        Collectors.summingDouble(expense -> Double.parseDouble(expense.getAmount()))));

        // Convert map to list of PieEntry
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey())); // Cast Double to float
        }

        return entries;
    }
}
