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

import uqac.dim.gestion_finance.adapters.UserBudgetAdapter;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Budget;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class BudgetActivity extends AppCompatActivity {

    private static final String TAG = "BudgetActivity";

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddBudget;
    private RecyclerView recyclerViewBudgets;
    private UserBudgetAdapter budgetAdapter;
    private AppDatabase db;

    private View textViewNoBudgets; // Vue pour afficher "Aucun budget"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        Log.d(TAG, "BudgetActivity started");

        initializeViews();
        setupNavigation();
        setupFab();

        // Initialiser la base de données
        db = AppDatabase.getDatabase(getApplicationContext());

        // Configurer le RecyclerView
        setupRecyclerView();

        // Charger les budgets
        loadBudgets();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAddBudget = findViewById(R.id.fabAddBudget);
        recyclerViewBudgets = findViewById(R.id.recyclerViewBudgets);
        textViewNoBudgets = findViewById(R.id.textViewNoBudgets); // Lier la vue "Aucun budget"

        bottomNavigation.setSelectedItemId(R.id.navigation_budget);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(BudgetActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                startActivity(new Intent(BudgetActivity.this, TransactionActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_budget) {
                Log.d(TAG, "setupNavigation: Already on BudgetActivity");
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                startActivity(new Intent(BudgetActivity.this, ParametresActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        fabAddBudget.setOnClickListener(v -> {
            Log.d(TAG, "FAB clicked to add a new budget");
            startActivity(new Intent(this, AjouterBudgetActivity.class));
        });
    }

    private void setupRecyclerView() {
        // Récupérer la devise actuelle depuis les paramètres
        GlobalSettings globalSettings = new GlobalSettings(this);
        String currentCurrency = globalSettings.getCurrency(); // Charge la devise depuis GlobalSettings

        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBudgets.setItemAnimator(new DefaultItemAnimator()); // Animation fluide

        // Passe les quatre paramètres requis : Context, Budgets (null pour l'initialisation), Listener, Devise
        budgetAdapter = new UserBudgetAdapter(this, null, this::onLastBudgetRemoved, currentCurrency);
        recyclerViewBudgets.setAdapter(budgetAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void loadBudgets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Budget> budgets = db.budgetDao().getAllBudgets();
            runOnUiThread(() -> {
                if (budgets != null && !budgets.isEmpty()) {
                    textViewNoBudgets.setVisibility(View.GONE);
                    recyclerViewBudgets.setVisibility(View.VISIBLE);
                    budgetAdapter.setBudgets(budgets);
                } else {
                    showNoBudgetsMessage();
                }
            });
        });
    }

    public void onLastBudgetRemoved() {
        Log.d(TAG, "Last budget removed");
        showNoBudgetsMessage();
    }

    private void showNoBudgetsMessage() {
        textViewNoBudgets.setVisibility(View.VISIBLE);
        recyclerViewBudgets.setVisibility(View.GONE);
    }

    public void attemptDeleteBudget(int budgetId, String budgetName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Récupérer les transactions associées à ce budget
            List<UserTransaction> transactions = db.transactionDao().getTransactionsByBudgetId(budgetId);

            runOnUiThread(() -> {
                if (transactions != null && !transactions.isEmpty()) {
                    // Transactions associées, empêcher la suppression et afficher l'alerte
                    showTransactionAlertDialog(budgetName, transactions);
                } else {
                    // Aucune transaction, confirmer la suppression
                    confirmBudgetDeletion(budgetId, budgetName);
                }
            });
        });
    }

    private void showTransactionAlertDialog(String budgetName, List<UserTransaction> transactions) {
        StringBuilder message = new StringBuilder(getString(R.string.cannot_delete_budget_with_transactions));
        message.append("\n\n");

        // Construire la liste des transactions
        for (UserTransaction transaction : transactions) {
            message.append("- ").append(transaction.Nom_transaction)
                    .append(" (").append(transaction.Date_transaction).append(")\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cannot_delete_budget, budgetName))
                .setMessage(message.toString())
                .setPositiveButton(getString(R.string.ok), null) // Fermer l'alerte
                .show();
    }

    private void confirmBudgetDeletion(int budgetId, String budgetName) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_budget, budgetName))
                .setMessage(R.string.delete_budget_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.budgetDao().deleteBudgetById(budgetId);
                            runOnUiThread(() -> {
                                Log.d(TAG, "Budget supprimé avec succès : " + budgetName);
                                refreshBudgetList();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur lors de la suppression du budget", e);
                            runOnUiThread(() -> {
                                showDeletionErrorDialog();
                            });
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeletionErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(R.string.cannot_delete_budget_due_to_transactions)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void refreshBudgetList() {
        // Méthode pour rafraîchir la liste des budgets après suppression
        loadBudgets();
    }
}