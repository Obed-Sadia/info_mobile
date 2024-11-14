package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import uqac.dim.gestion_finance.entities.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Query("SELECT * FROM Budget")
    List<Budget> getAll();

    @Query("SELECT * FROM Budget WHERE ID_Budget = :id")
    Budget getById(int id);

    @Query("DELETE FROM Budget")
    void deleteAll();
}