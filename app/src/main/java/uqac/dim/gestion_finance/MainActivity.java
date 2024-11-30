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

        // GlobalSettings s'applique automatiquement via MyApplication
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
                // Déjà sur MainActivity, aucune action nécessaire
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                // Naviguer vers TransactionActivity
                Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer MainActivity
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // Naviguer vers BudgetActivity
                Intent intent = new Intent(MainActivity.this, BudgetActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer MainActivity
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                // Naviguer vers ParametresActivity
                Intent intent = new Intent(MainActivity.this, ParametresActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer MainActivity
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
            registerReceiver(settingsReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
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
            Log.e(TAG, getString(R.string.error_invalid_user_id));
            return;
        }
        new Thread(() -> {
            final Utilisateur user = utilisateurDao.getById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    welcomeMessage.setText(getString(R.string.welcome_user, user.Nom));
                    Log.d(TAG, getString(R.string.user_data_loaded, user.Nom));
                    loadRecentTransactions(); // Charger les transactions après le chargement des données utilisateur
                } else {
                    Log.e(TAG, getString(R.string.error_user_not_found));
                }
            });
        }).start();
    }

    private void loadRecentTransactions() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Log.e(TAG, getString(R.string.error_invalid_user_id));
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
                    Log.d(TAG, getString(R.string.transactions_loaded, recentTransactions.size(), userId));
                } else {
                    recentTransactionsList.setVisibility(View.GONE);
                    noTransactionsMessage.setText(getString(R.string.no_transactions));
                    noTransactionsMessage.setVisibility(View.VISIBLE);
                    Log.d(TAG, getString(R.string.no_recent_transactions, userId));
                }
            });
        }).start();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);
        Log.d("MainActivity", "Retrieved User ID: " + userId);
        return userId;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        GlobalSettings globalSettings = new GlobalSettings(newBase);
        String language = globalSettings.getLanguage(); // Récupère la langue sauvegardée
        Context context = LocaleHelper.wrap(newBase, language); // Applique la langue
        super.attachBaseContext(context);
    }
}