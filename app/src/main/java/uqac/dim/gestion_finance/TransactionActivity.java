package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.adapters.UserTransactionAdapter;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "TransactionActivity";

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddTransaction;
    private RecyclerView recyclerViewTransactions;

    private UserTransactionAdapter transactionAdapter;
    private AppDatabase db;

    private View textViewNoTransactions; // Vue pour afficher "Aucune transaction"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        Log.d(TAG, "onCreate: TransactionActivity started");

        initializeViews();
        setupNavigation();
        setupFab();

        // Initialiser la base de données
        db = AppDatabase.getDatabase(getApplicationContext());

        // Configurer le RecyclerView
        setupRecyclerView();

        // Charger les transactions
        loadTransactions();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        textViewNoTransactions = findViewById(R.id.textViewNoTransactions); // Lier la vue "Aucune transaction"

        if (bottomNavigation == null) {
            Log.e(TAG, "initializeViews: BottomNavigation is null. Interface initialization failed.");
            finish();
        } else {
            // Sélectionner l'élément correspondant à TransactionActivity
            bottomNavigation.setSelectedItemId(R.id.navigation_transaction);
        }
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(TransactionActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer TransactionActivity
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                // Déjà sur TransactionActivity, aucune action nécessaire
                Log.d(TAG, "setupNavigation: Already on TransactionActivity");
                return true;
            } else if (itemId == R.id.navigation_budget) {
                Intent intent = new Intent(TransactionActivity.this, BudgetActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer TransactionActivity
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                Intent intent = new Intent(TransactionActivity.this, ParametresActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer TransactionActivity
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        fabAddTransaction.setOnClickListener(v -> {
            Log.d(TAG, "setupFab: FloatingActionButton clicked");

            // Redirection vers AjouterTransactionActivity
            Intent intent = new Intent(TransactionActivity.this, AjouterTransactionActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setItemAnimator(new DefaultItemAnimator());
        transactionAdapter = new UserTransactionAdapter(this, null, new UserTransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onEditTransaction(UserTransaction transaction) {
                // Redirection vers EditTransactionActivity
                Intent intent = new Intent(TransactionActivity.this, EditerTransactionActivity.class);
                intent.putExtra("transactionId", transaction.ID_Transaction);
                startActivity(intent);
            }

            @Override
            public void onDeleteTransaction(UserTransaction transaction) {
                deleteTransaction(transaction);
            }
        });
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void deleteTransaction(UserTransaction transaction) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.transactionDao().deleteTransactionById(transaction.ID_Transaction);
            runOnUiThread(() -> {
                loadTransactions(); // Rafraîchir les données après suppression
                Log.d(TAG, "deleteTransaction: Transaction deleted successfully");
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions(); // Recharger les transactions chaque fois que l'activité est reprise
    }

    private void loadTransactions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserTransaction> transactions = db.transactionDao().getAll(); // Charger les transactions
            runOnUiThread(() -> {
                if (transactions != null && !transactions.isEmpty()) {
                    Log.d(TAG, "loadTransactions: Transactions loaded");
                    textViewNoTransactions.setVisibility(View.GONE); // Masquer le texte "Aucune transaction"
                    recyclerViewTransactions.setVisibility(View.VISIBLE); // Afficher le RecyclerView
                    transactionAdapter.setTransactions(transactions); // Mettre à jour les données de l'adaptateur
                } else {
                    Log.w(TAG, "loadTransactions: No transactions found");
                    showNoTransactionsMessage();
                }
            });
        });
    }

    private void showNoTransactionsMessage() {
        textViewNoTransactions.setVisibility(View.VISIBLE); // Afficher le message
        recyclerViewTransactions.setVisibility(View.GONE); // Masquer le RecyclerView
    }
}