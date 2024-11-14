package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import uqac.dim.gestion_finance.entities.Categorie;

import java.util.List;

@Dao
public interface CategorieDao {
    @Insert
    void insert(Categorie categorie);

    @Query("SELECT * FROM Categorie")
    List<Categorie> getAll();

    @Query("SELECT * FROM Categorie WHERE ID_Categorie = :id")
    Categorie getById(int id);

    @Query("DELETE FROM Categorie")
    void deleteAll();
}