package com.example.budgettracker;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddExpense())
                    .commit();
            toolbar.setTitle("Add Expense");
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            if (item.getItemId() == R.id.nav_add_expense) {
                selectedFragment = new AddExpense();
                title = "Add Expense";
            } else if (item.getItemId() == R.id.nav_summary) {
                selectedFragment = new Summary();
                title = "Summary";
            } else if (item.getItemId() == R.id.nav_budget) {
                selectedFragment = new BudgetManagement();
                title = "Budget Management";
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                toolbar.setTitle(title);
            }
            return true;
        });
    }
}
