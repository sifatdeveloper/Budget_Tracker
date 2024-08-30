package com.example.budgettracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(Expense expense);

    @Query("SELECT * FROM expenses")
    List<Expense> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE id = :id")
    Expense getExpenseById(int id);

    @Update
    void updateExpense(Expense expense);

    @Delete
    void deleteExpense(Expense expense);

    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteExpenseById(int id);
    @Query("SELECT SUM(CAST(amount AS REAL)) FROM expenses WHERE type = 'Income'")
    float getTotalIncome();

    @Query("SELECT SUM(CAST(amount AS REAL)) FROM expenses WHERE type = 'Expense'")
    float getTotalExpense();
    @Query("DELETE FROM expenses")
    void deleteAllExpenses();
}
