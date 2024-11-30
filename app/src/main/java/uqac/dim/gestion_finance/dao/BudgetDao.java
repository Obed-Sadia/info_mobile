package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import uqac.dim.gestion_finance.entities.Budget;

@Dao
public interface BudgetDao {

    // Insérer un nouveau budget
    @Insert
    void insert(Budget budget);

    // Mettre à jour un budget existant
    @Update
    void update(Budget budget);

    // Supprimer un budget
    @Delete
    void delete(Budget budget);

    // Récupérer tous les budgets
    @Query("SELECT * FROM Budget")
    List<Budget> getAllBudgets();

    // Récupérer un budget par ID
    @Query("SELECT * FROM Budget WHERE id = :budgetId LIMIT 1")
    Budget getBudgetById(int budgetId);

    // Récupérer tous les budgets actifs
    @Query("SELECT * FROM Budget WHERE actif = 1")
    List<Budget> getActiveBudgets();

    // Supprimer tous les budgets
    @Query("DELETE FROM Budget")
    void deleteAllBudgets();

    @Query("SELECT * FROM budget WHERE nom = :nom LIMIT 1")
    Budget getBudgetByName(String nom);
}