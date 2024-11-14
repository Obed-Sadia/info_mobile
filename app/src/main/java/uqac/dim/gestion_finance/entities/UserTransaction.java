package uqac.dim.gestion_finance.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "UserTransaction") // Nouveau nom de table
public class UserTransaction {
    @PrimaryKey(autoGenerate = true)
    public int ID_Transaction;

    public int ID_Utilisateur; // Référence à Utilisateur

    public String Nom_transaction;

    public double Montant;

    public String Date_transaction; // Format YYYY-MM-DD

    public int ID_Categorie; // Référence à Categorie

    public int ID_Mode; // Référence à ModePaiement

    public boolean Recurrence;
}