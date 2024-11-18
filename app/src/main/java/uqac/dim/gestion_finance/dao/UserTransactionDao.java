package uqac.dim.gestion_finance.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import uqac.dim.gestion_finance.entities.UserTransaction; // Mettez à jour l'importation

import java.util.List;

@Dao
public interface UserTransactionDao { // Mettez à jour le nom de l'interface
    @Insert
    void insert(UserTransaction transaction);

    @Query("SELECT * FROM UserTransaction") // Mettez à jour le nom de la table
    List<UserTransaction> getAll();

    @Query("SELECT * FROM UserTransaction WHERE ID_Transaction = :id") // Mettez à jour le nom de la table
    UserTransaction getById(int id);

    @Query("DELETE FROM UserTransaction") // Mettez à jour le nom de la table
    void deleteAll();

    @Query("SELECT * FROM Usertransaction WHERE ID_Utilisateur = :userId ORDER BY Date_transaction DESC LIMIT :limit")
    List<UserTransaction> getRecentTransactions(int userId, int limit);
}
