package uqac.dim.gestion_finance.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Categorie {
    @PrimaryKey(autoGenerate = true)
    public int ID_Categorie;

    public String Nom_categorie;
}