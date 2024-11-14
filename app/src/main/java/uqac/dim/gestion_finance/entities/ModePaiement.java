package uqac.dim.gestion_finance.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ModePaiement {
    @PrimaryKey(autoGenerate = true)
    public int ID_Mode;

    public String Nom_mode;
}