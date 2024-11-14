package uqac.dim.gestion_finance.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Parametres {
    @PrimaryKey(autoGenerate = true)
    public int ID_Parametres;

    public int ID_Utilisateur; // Référence à Utilisateur

    public String Langue;

    public String Devise;

    public boolean Mode_sombre;

    public boolean Notifications;
}