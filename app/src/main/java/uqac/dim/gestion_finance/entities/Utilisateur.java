package uqac.dim.gestion_finance.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Utilisateur",
        indices = {@Index(value = "Email", unique = true)} // Contrainte d'unicité sur Email
)
public class Utilisateur {
    @PrimaryKey(autoGenerate = true)
    public int ID_Utilisateur;

    public String Nom;

    @ColumnInfo(name = "Email")
    public String Email;

    public String Mot_de_passe;

    public String Date_inscription; // Format YYYY-MM-DD
}