package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import uqac.dim.gestion_finance.entities.Categorie;

@Dao
public interface CategorieDao {

    @Insert
    void insert(Categorie categorie);

    @Query("SELECT * FROM Categorie")
    List<Categorie> getAllCategories();

    @Query("DELETE FROM Categorie")
    void deleteAllCategories();

    @Query("SELECT * FROM categorie WHERE nom = :nom LIMIT 1")
    Categorie getCategorieByName(String nom);
}