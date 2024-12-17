package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    private RecyclerView recyclerViewTransactions;
    private TextView noTransactionsMessage;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigation;

    private UserTransactionAdapter transactionAdapter;
    private List<UserTransaction> transactionsList;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        initializeViews();
        setupNavigation();
        setupRecyclerView();
        setupFab();

        db = AppDatabase.getDatabase(getApplicationContext());

        loadTransactions();
    }

    private void initializeViews() {
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        noTransactionsMessage = findViewById(R.id.textViewNoTransactions);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.navigation_transaction);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                return true; // Déjà sur cette page
            } else if (itemId == R.id.navigation_budget) {
                startActivity(new Intent(this, BudgetActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                startActivity(new Intent(this, ParametresActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setHasFixedSize(true);

        transactionAdapter = new UserTransactionAdapter(
                this,
                transactionsList,
                new UserTransactionAdapter.OnTransactionActionListener() {
                    @Override
                    public void onEditTransaction(UserTransaction transaction) {
                        // Logique pour éditer la transaction
                        Intent intent = new Intent(TransactionActivity.this, EditerTransactionActivity.class);
                        intent.putExtra("transactionId", transaction.ID_Transaction);
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteTransaction(UserTransaction transaction) {
                        Log.d(TAG, "Transaction supprimée : " + transaction.Nom_transaction);
                    }
                },
                () -> {
                    // Callback si la dernière transaction est supprimée
                    noTransactionsMessage.setVisibility(View.VISIBLE);
                    recyclerViewTransactions.setVisibility(View.GONE);
                }
        );

        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void setupFab() {
        fabAddTransaction.setOnClickListener(v -> {
            // Rediriger vers l'activité d'ajout de transaction
            startActivityForResult(new Intent(this, AjouterTransactionActivity.class), 1);
        });
    }

    private void loadTransactions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            transactionsList = db.transactionDao().getAll(); // Charger toutes les transactions
            runOnUiThread(() -> {
                if (transactionsList != null && !transactionsList.isEmpty()) {
                    noTransactionsMessage.setVisibility(View.GONE);
                    recyclerViewTransactions.setVisibility(View.VISIBLE);
                    transactionAdapter.setTransactions(transactionsList);
                } else {
                    noTransactionsMessage.setVisibility(View.VISIBLE);
                    recyclerViewTransactions.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions(); // Recharger les transactions après retour sur l'écran
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadTransactions(); // Recharger les données après ajout
        }
    }
}