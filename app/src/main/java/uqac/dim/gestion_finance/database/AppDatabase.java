package uqac.dim.gestion_finance.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import uqac.dim.gestion_finance.dao.UserTransactionDao;
import uqac.dim.gestion_finance.dao.UtilisateurDao;
import uqac.dim.gestion_finance.dao.CategorieDao;
import uqac.dim.gestion_finance.dao.ModePaiementDao;
import uqac.dim.gestion_finance.dao.BudgetDao;
import uqac.dim.gestion_finance.dao.ParametresDao;
import uqac.dim.gestion_finance.dao.SessionTokenDao; // Import du DAO
import uqac.dim.gestion_finance.entities.UserTransaction;
import uqac.dim.gestion_finance.entities.Utilisateur;
import uqac.dim.gestion_finance.entities.Categorie;
import uqac.dim.gestion_finance.entities.ModePaiement;
import uqac.dim.gestion_finance.entities.Budget;
import uqac.dim.gestion_finance.entities.Parametres;
import uqac.dim.gestion_finance.entities.SessionToken; // Import de l'entité

@Database(entities = {Utilisateur.class, Categorie.class, ModePaiement.class, UserTransaction.class,
        Budget.class, Parametres.class, SessionToken.class}, // Ajout de SessionToken
        version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UtilisateurDao utilisateurDao();

    public abstract CategorieDao categorieDao();

    public abstract ModePaiementDao modePaiementDao();

    public abstract UserTransactionDao transactionDao();

    public abstract BudgetDao budgetDao();

    public abstract ParametresDao parametresDao();

    public abstract SessionTokenDao sessionTokenDao(); // Ajout de la méthode DAO

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "finance_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}