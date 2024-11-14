package uqac.dim.gestion_finance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

import uqac.dim.gestion_finance.adapters.UserTransactionAdapter;
import uqac.dim.gestion_finance.dao.UserTransactionDao;
import uqac.dim.gestion_finance.dao.UtilisateurDao;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.UserTransaction;
import uqac.dim.gestion_finance.entities.Utilisateur;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    private RecyclerView recentTransactionsList;
    private TextView noTransactionsMessage;
    private BottomNavigationView bottomNavigation;

    private AppDatabase db;
    private UserTransactionDao userTransactionDao;
    private UtilisateurDao utilisateurDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        // Initialiser les vues
        welcomeMessage = findViewById(R.id.welcomeMessage);
        recentTransactionsList = findViewById(R.id.recentTransactionsList);
        noTransactionsMessage = findViewById(R.id.noTransactionsMessage);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Initialiser la base de données
        db = AppDatabase.getDatabase(getApplicationContext());
        userTransactionDao = db.transactionDao();
        utilisateurDao = db.utilisateurDao();

        // Configurer la RecyclerView
        recentTransactionsList.setLayoutManager(new LinearLayoutManager(this));

        // Charger les données
        loadUserData();
        loadRecentTransactions();

        // Configurer la navigation
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    // Déjà sur l'écran d'accueil
                    return true;
                } else if (itemId == R.id.navigation_transaction) {
                    // Naviguer vers l'écran de transaction
                    // startActivity(new Intent(HomeActivity.this, TransactionActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    // Naviguer vers l'écran de budget
                    // startActivity(new Intent(HomeActivity.this, BudgetActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_parametres) {
                    // Naviguer vers l'écran de paramètres
                    Intent intent = new Intent(MainActivity.this, ParametresActivity.class);
                    startActivity(intent);

                }
                return false;
            }
        });
    }

    private void loadUserData() {
        // Supposons que vous avez une méthode pour obtenir l'ID de l'utilisateur connecté
        int userId = getCurrentUserId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Utilisateur user = utilisateurDao.getById(userId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user != null) {
                            welcomeMessage.setText("Bienvenue, " + user.Nom + "!");
                        }
                    }
                });
            }
        }).start();
    }

    private void loadRecentTransactions() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<UserTransaction> recentTransactions = userTransactionDao.getRecentTransactions(3);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recentTransactions != null && !recentTransactions.isEmpty()) {
                            recentTransactionsList.setAdapter(new UserTransactionAdapter(recentTransactions));
                            recentTransactionsList.setVisibility(View.VISIBLE);
                            noTransactionsMessage.setVisibility(View.GONE);
                        } else {
                            recentTransactionsList.setVisibility(View.GONE);
                            noTransactionsMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    // Méthode fictive pour obtenir l'ID de l'utilisateur connecté
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);

        if (userId == -1) {
            // L'utilisateur n'est pas connecté ou l'ID n'a pas été sauvegardé
            // Vous pouvez gérer ce cas en redirigeant vers l'écran de connexion
            Intent loginIntent = new Intent(this, ConnexionActivity.class);
            startActivity(loginIntent);
            finish(); // Ferme l'activité actuelle
            return -1;
        }

        return userId;
    }
}