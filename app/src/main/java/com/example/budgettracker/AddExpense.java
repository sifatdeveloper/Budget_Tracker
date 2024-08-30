package com.example.budgettracker;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExpense extends Fragment {

    private ExpenseDao expenseDao;
    private Spinner spinner1, spinner2;
    private ArrayList<String> array1 = new ArrayList<>();
    private ArrayList<String> array2 = new ArrayList<>();
    private ArrayList<String> array3 = new ArrayList<>();
    private TextInputLayout textInputLayout;
    private FloatingActionButton floatingActionButton;
    private AppCompatButton button;
    private EditText editText;
    private CardView cardView;
    private TextInputEditText amount;
    private ExecutorService executorService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        spinner1 = view.findViewById(R.id.spinner);
        cardView = view.findViewById(R.id.card);
        spinner2 = view.findViewById(R.id.spinnercat);
        amount = view.findViewById(R.id.amount);
        editText = view.findViewById(R.id.date);
        button = view.findViewById(R.id.save);
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        textInputLayout = view.findViewById(R.id.text);

        setupViews();
        setupSpinnerAdapters();
        setupListeners();

        // Initialize ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        return view;
    }

    private void setupViews() {
        cardView.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        textInputLayout.setVisibility(View.GONE);
        spinner1.setVisibility(View.GONE);
        spinner2.setVisibility(View.GONE);
    }

    private void setupSpinnerAdapters() {
        array1.add("Income");
        array1.add("Expense");
        array2.add("Gift");
        array2.add("Milk");
        array2.add("Army pay");
        array3.add("Fuels");
        array3.add("Fee");
        array3.add("Shop return");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array1);
        spinner1.setAdapter(adapter);
    }

    private void setupListeners() {
        floatingActionButton.setOnClickListener(v -> {
            cardView.setVisibility(View.VISIBLE);
            spinner1.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
            spinner2.setVisibility(View.VISIBLE);
            textInputLayout.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = array1.get(position);
                ArrayAdapter<String> adapter;
                if (item.equals("Income")) {
                    adapter = new ArrayAdapter<>(getActivity(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array2);
                } else {
                    adapter = new ArrayAdapter<>(getActivity(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array3);
                }
                spinner2.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        button.setOnClickListener(v -> saveExpense());
        editText.setOnClickListener(v -> showDatePickerDialog());
    }

    private void saveExpense() {
        String amountStr = amount.getText().toString().trim();
        String dateStr = editText.getText().toString().trim();
        String type = spinner1.getSelectedItem().toString();
        String category = spinner2.getSelectedItem().toString();

        if (!amountStr.isEmpty() && !dateStr.isEmpty()) {
            double amountVal = Double.parseDouble(amountStr);

            executorService.execute(() -> {
                // Retrieve total income from database
                ExpenseDatabase db = ExpenseDatabase.getInstance(getContext());
                List<Expense> allExpenses = db.expenseDao().getAllExpenses();
                double totalIncome = 0;
                for (Expense expense : allExpenses) {
                    if (expense.getType().equals("Income")) {
                        totalIncome += Double.parseDouble(expense.getAmount());
                    }
                }

                boolean canAddExpense = !type.equals("Expense") || amountVal <= totalIncome;

                if (canAddExpense) {
                    Expense expense = new Expense(type, category, amountStr, dateStr);
                    db.expenseDao().insertExpense(expense);
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show());
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Expense cannot exceed total income", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(selectedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }
}
