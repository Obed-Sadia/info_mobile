package uqac.dim.gestion_finance.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "SessionToken")
public class SessionToken {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId; // ID utilisateur associé au token
    public String token; // Token de session unique
    public long timestamp; // Timestamp pour vérifier la validité
}
