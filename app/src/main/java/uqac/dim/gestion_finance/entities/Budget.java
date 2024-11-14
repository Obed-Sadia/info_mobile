package uqac.dim.gestion_finance.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int ID_Budget;

    public int ID_Utilisateur; // Référence à Utilisateur

    public double Montant_budget;

    public String Date_debut; // Format YYYY-MM-DD

    public String Date_fin; // Format YYYY-MM-DD

    public String Nom_budget;
}