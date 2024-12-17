package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

import uqac.dim.gestion_finance.entities.UserTransaction;

@Dao
public interface UserTransactionDao {

    // Insérer une nouvelle transaction
    @Insert
    void insert(UserTransaction transaction);

    // Mettre à jour une transaction existante
    @Update
    void update(UserTransaction transaction);

    // Supprimer une transaction par ID
    @Query("DELETE FROM UserTransaction WHERE ID_Transaction = :transactionId")
    void deleteTransactionById(int transactionId);

    // Récupérer toutes les transactions
    @Query("SELECT * FROM UserTransaction")
    List<UserTransaction> getAll();

    // Récupérer une transaction par ID
    @Query("SELECT * FROM UserTransaction WHERE ID_Transaction = :id")
    UserTransaction getById(int id);

    // Récupérer les transactions par budget (ID_Categorie)
    @Query("SELECT * FROM UserTransaction WHERE ID_Categorie = :budgetId")
    List<UserTransaction> getTransactionsByBudgetId(int budgetId);

    // Supprimer toutes les transactions
    @Query("DELETE FROM UserTransaction")
    void deleteAll();

    // Supprimer les transactions par budget
    @Query("DELETE FROM UserTransaction WHERE ID_Categorie = :budgetId")
    void deleteTransactionsByBudgetId(int budgetId);

    // Récupérer les transactions récentes pour un utilisateur
    @Query("SELECT * FROM UserTransaction WHERE ID_Utilisateur = :userId ORDER BY Date_transaction DESC LIMIT :limit")
    List<UserTransaction> getRecentTransactions(int userId, int limit);

    // Obtenir le total des montants pour un budget spécifique
    @Query("SELECT SUM(Montant) FROM UserTransaction WHERE ID_Categorie = :budgetId")
    Double getTotalAmountByBudgetId(int budgetId);
}