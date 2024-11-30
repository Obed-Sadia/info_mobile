package uqac.dim.gestion_finance.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Budget")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nom; // Nom du budget
    public double montant; // Montant alloué au budget
    public String categorie; // Catégorie associée au budget
    public String recurrence; // Type de récurrence : none, monthly, yearly
    public boolean actif; // Indique si le budget est actif ou non

    // Constantes pour les types de récurrence
    public static final String RECURRENCE_NONE = "none";
    public static final String RECURRENCE_MONTHLY = "monthly";
    public static final String RECURRENCE_YEARLY = "yearly";

    // Constructeur
    public Budget(String nom, double montant, String categorie, String recurrence, boolean actif) {
        this.nom = nom;
        this.montant = montant;
        this.categorie = categorie;
        this.recurrence = normalizeRecurrence(recurrence);
        this.actif = actif;
    }

    // Méthode pour normaliser la récurrence
    private String normalizeRecurrence(String recurrence) {
        if (recurrence == null) {
            return RECURRENCE_NONE;
        }
        switch (recurrence.toLowerCase()) {
            case "mensuelle":
            case "monthly":
                return RECURRENCE_MONTHLY;
            case "annuelle":
            case "yearly":
                return RECURRENCE_YEARLY;
            case "aucune":
            case "none":
                return RECURRENCE_NONE;
            default:
                return recurrence; // Retourne la valeur brute si elle n'est pas reconnue
        }
    }
}