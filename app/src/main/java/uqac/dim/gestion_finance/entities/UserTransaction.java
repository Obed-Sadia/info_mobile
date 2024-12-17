package uqac.dim.gestion_finance.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "UserTransaction",
        foreignKeys = @ForeignKey(
                entity = Budget.class,
                parentColumns = "id",        // Clé primaire de Budget
                childColumns = "ID_Categorie", // Colonne référence dans UserTransaction
                onDelete = ForeignKey.RESTRICT // Empêche la suppression d'un budget lié
        )
)
public class UserTransaction {
    @PrimaryKey(autoGenerate = true)
    public int ID_Transaction;

    public int ID_Utilisateur; // Référence à Utilisateur

    public String Nom_transaction;

    public double Montant;

    public String Date_transaction; // Format YYYY-MM-DD

    public int ID_Categorie; // Référence à Budget

    public int ID_Mode; // Référence à ModePaiement

    public boolean Recurrence;
}