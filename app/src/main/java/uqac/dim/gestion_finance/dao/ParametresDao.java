package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import uqac.dim.gestion_finance.entities.Parametres;

@Dao
public interface ParametresDao {
    @Insert
    void insert(Parametres parametres);

    @Query("SELECT * FROM Parametres WHERE ID_Utilisateur = :userId")
    Parametres getByUserId(int userId);

    @Query("DELETE FROM Parametres WHERE ID_Utilisateur = :userId")
    void deleteByUserId(int userId);

    @Update
    void update(Parametres parametres);
}
