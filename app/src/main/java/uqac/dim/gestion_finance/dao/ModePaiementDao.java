package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import uqac.dim.gestion_finance.entities.ModePaiement;

import java.util.List;

@Dao
public interface ModePaiementDao {
    @Insert
    void insert(ModePaiement modePaiement);

    @Query("SELECT * FROM ModePaiement")
    List<ModePaiement> getAll();

    @Query("SELECT * FROM ModePaiement WHERE ID_Mode = :id")
    ModePaiement getById(int id);

    @Query("DELETE FROM ModePaiement")
    void deleteAll();
}