package uqac.dim.gestion_finance.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Categorie")
public class Categorie {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nom;

    public Categorie(String nom) {
        this.nom = nom;
    }
}