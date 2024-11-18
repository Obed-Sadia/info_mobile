package uqac.dim.gestion_finance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import uqac.dim.gestion_finance.adapters.UserTransactionAdapter;
import uqac.dim.gestion_finance.dao.UserTransactionDao;
import uqac.dim.gestion_finance.dao.UtilisateurDao;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.UserTransaction;
import uqac.dim.gestion_finance.entities.Utilisateur;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView welcomeMessage;
    private RecyclerView recentTransactionsList;
    private TextView noTransactionsMessage;
    private BottomNavigationView bottomNavigation;

    private AppDatabase db;
    private UserTransactionDao userTransactionDao;
    private UtilisateurDao utilisateurDao;
    private UserTransactionAdapter transactionAdapter;

    private BroadcastReceiver settingsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);
        Log.d(TAG, "onCreate: MainActivity started");

        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        loadUserData();
        loadRecentTransactions();
        setupNavigation();
        initializeSettingsReceiver();

        Log.d(TAG, "onCreate: MainActivity setup completed");
    }

    private void initializeViews() {
        welcomeMessage = findViewById(R.id.welcomeMessage);
        recentTransactionsList = findViewById(R.id.recentTransactionsList);
        noTransactionsMessage = findViewById(R.id.noTransactionsMessage);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (welcomeMessage == null || recentTransactionsList == null || noTransactionsMessage == null || bottomNavigation == null) {
            Log.e(TAG, "initializeViews: One or more views are null");
            Toast.makeText(this, "Erreur lors de l'initialisation de l'interface", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeDatabase() {
        db = AppDatabase.getDatabase(getApplicationContext());
        userTransactionDao = db.transactionDao();
        utilisateurDao = db.utilisateurDao();
    }

    private void setupRecyclerView() {
        recentTransactionsList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                //Toast.makeText(this, "Navigation vers TransactionActivity", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_budget) {
                //Toast.makeText(this, "Navigation vers BudgetActivity", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                startActivity(new Intent(MainActivity.this, ParametresActivity.class));
                return true;
            }
            return false;
        });
    }

    private void initializeSettingsReceiver() {
        settingsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("SETTINGS_UPDATED".equals(intent.getAction())) {
                    updateUI();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("SETTINGS_UPDATED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(settingsReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(settingsReceiver, filter);
        }
        updateUI();
        Log.d(TAG, "onResume: MainActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(settingsReceiver);
        Log.d(TAG, "onPause: MainActivity paused");
    }

    private void updateUI() {
        loadUserData();
        if (transactionAdapter != null) {
            transactionAdapter.updateCurrency();
        }
        Log.d(TAG, "updateUI: UI updated");
    }

    private void loadUserData() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Log.e(TAG, "loadUserData: ID utilisateur invalide");
            return;
        }
        new Thread(() -> {
            final Utilisateur user = utilisateurDao.getById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    welcomeMessage.setText("Bienvenue, " + user.Nom + "!");
                    Log.d(TAG, "loadUserData: Données utilisateur chargées pour " + user.Nom);
                    loadRecentTransactions(); // Charger les transactions après le chargement des données utilisateur
                } else {
                    Log.e(TAG, "loadUserData: Utilisateur non trouvé");
                    Toast.makeText(this, "Erreur: Utilisateur non trouvé", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void loadRecentTransactions() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Log.e(TAG, "loadRecentTransactions: ID utilisateur invalide");
            return;
        }

        new Thread(() -> {
            final List<UserTransaction> recentTransactions = userTransactionDao.getRecentTransactions(userId, 3);
            runOnUiThread(() -> {
                if (recentTransactions != null && !recentTransactions.isEmpty()) {
                    transactionAdapter = new UserTransactionAdapter(this, recentTransactions);
                    recentTransactionsList.setAdapter(transactionAdapter);
                    recentTransactionsList.setVisibility(View.VISIBLE);
                    noTransactionsMessage.setVisibility(View.GONE);
                    Log.d(TAG, "loadRecentTransactions: Chargement de " + recentTransactions.size() + " transactions pour l'utilisateur " + userId);
                } else {
                    recentTransactionsList.setVisibility(View.GONE);
                    noTransactionsMessage.setVisibility(View.VISIBLE);
                    Log.d(TAG, "loadRecentTransactions: Aucune transaction récente trouvée pour l'utilisateur " + userId);
                }
            });
        }).start();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);

        if (userId == -1) {
            Log.e(TAG, "getCurrentUserId: User ID not found in SharedPreferences");
            Intent loginIntent = new Intent(this, ConnexionActivity.class);
            startActivity(loginIntent);
            finish();
        }

        return userId;
    }
}