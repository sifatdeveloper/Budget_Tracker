package com.example.budgettracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetManagement extends Fragment {

    private TextView totalIncomeTextView;
    private TextView remainingIncomeTextView1,remainingIncomeTextView2;
    private TextView remainingIncomeTextView;
    private RecyclerView expenseCategoryRecyclerView;
    private ExpenseDao expenseDao;
    private ExpenseCategoryAdapter adapter;
    private Button clearDataButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_management, container, false);

        totalIncomeTextView = view.findViewById(R.id.totalIncomeTextView);
        remainingIncomeTextView = view.findViewById(R.id.remainingIncomeTextView);
        expenseCategoryRecyclerView = view.findViewById(R.id.expenseCategoryRecyclerView);
        clearDataButton = view.findViewById(R.id.clearDataButton);
        remainingIncomeTextView1=view.findViewById(R.id.totalIncomeTextView1);
        remainingIncomeTextView2=view.findViewById(R.id.remainingIncomeTextView1);

        // Set up RecyclerView
        expenseCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize ExpenseDao
        expenseDao = ExpenseDatabase.getInstance(getContext()).expenseDao();

        // Fetch and display data
        fetchData();

        // Set up the clear data button click listener
        clearDataButton.setOnClickListener(v -> clearAllData());

        return view;
    }

    private void fetchData() {
        new Thread(() -> {
            List<Expense> allExpenses = expenseDao.getAllExpenses();
            float totalIncome = getTotalIncome(allExpenses);
            float totalExpense = getTotalExpense(allExpenses);
            float remainingIncome = totalIncome - totalExpense;

            getActivity().runOnUiThread(() -> {
                totalIncomeTextView.setText("Total Income: ");
                remainingIncomeTextView.setText("$"+totalIncome);
                remainingIncomeTextView1.setText("Remaining Income:");
                remainingIncomeTextView2.setText("$"+remainingIncome);
                setupExpenseCategoryRecyclerView(allExpenses);
            });
        }).start();
    }

    private float getTotalIncome(List<Expense> expenses) {
        return (float) expenses.stream()
                .filter(expense -> expense.getType().equals("Income"))
                .mapToDouble(expense -> Double.parseDouble(expense.getAmount()))
                .sum();
    }

    private float getTotalExpense(List<Expense> expenses) {
        return (float) expenses.stream()
                .filter(expense -> expense.getType().equals("Expense"))
                .mapToDouble(expense -> Double.parseDouble(expense.getAmount()))
                .sum();
    }

    private void setupExpenseCategoryRecyclerView(List<Expense> expenses) {
        Map<String, Float> categoryTotals = expenses.stream()
                .filter(expense -> expense.getType().equals("Expense"))
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(expense -> Double.parseDouble(expense.getAmount()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().floatValue()
                ));

        List<ExpenseCategory> categories = categoryTotals.entrySet().stream()
                .map(entry -> new ExpenseCategory(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        adapter = new ExpenseCategoryAdapter(categories);
        expenseCategoryRecyclerView.setAdapter(adapter);
    }

    private void clearAllData() {
        new Thread(() -> {
            expenseDao.deleteAllExpenses();  // Clears all data from the database
            getActivity().runOnUiThread(() -> {
                totalIncomeTextView.setText("Total Income: $0.00");
                remainingIncomeTextView.setText("Remaining Income: $0.00");
                adapter.clearData();  // Clears the adapter data
            });
        }).start();
    }

    // Inner class for ExpenseCategory
    public static class ExpenseCategory {
        private String category;
        private float amount;

        public ExpenseCategory(String category, float amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public float getAmount() {
            return amount;
        }
    }
}
