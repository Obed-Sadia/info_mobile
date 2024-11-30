package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
        Log.d(getString(R.string.log_tag_budget_activity), getString(R.string.log_activity_started));

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
                Intent intent = new Intent(BudgetActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer BudgetActivity
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                Intent intent = new Intent(BudgetActivity.this, TransactionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer BudgetActivity
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // Déjà sur BudgetActivity, aucune action nécessaire
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                Intent intent = new Intent(BudgetActivity.this, ParametresActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish(); // Terminer BudgetActivity
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        fabAddBudget.setOnClickListener(v -> {
            Log.d(getString(R.string.log_tag_budget_activity), getString(R.string.log_fab_clicked));
            Intent intent = new Intent(BudgetActivity.this, AjouterBudgetActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        // Récupérer la devise actuelle depuis les paramètres
        GlobalSettings globalSettings = new GlobalSettings(this);
        String currentCurrency = globalSettings.getCurrency(); // Charge la devise depuis GlobalSettings

        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBudgets.setItemAnimator(new DefaultItemAnimator()); // Animation fluide
        budgetAdapter = new UserBudgetAdapter(this, null, this::onLastBudgetRemoved, currentCurrency); // Passe la devise
        recyclerViewBudgets.setAdapter(budgetAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalSettings globalSettings = new GlobalSettings(this);
        String currentCurrency = globalSettings.getCurrency();
        Log.d(getString(R.string.log_tag_budget_activity), getString(R.string.log_current_currency, currentCurrency));
        if (budgetAdapter != null) {
            budgetAdapter.setCurrency(currentCurrency);
        }
        loadBudgets();
    }

    private void loadBudgets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Budget> budgets = db.budgetDao().getAllBudgets(); // Charger les budgets depuis la BD
            GlobalSettings globalSettings = new GlobalSettings(this);
            String currentCurrency = globalSettings.getCurrency(); // Obtenir la devise mise à jour
            runOnUiThread(() -> {
                if (budgets != null && !budgets.isEmpty()) {
                    Log.d(getString(R.string.log_tag_budget_activity), getString(R.string.log_budgets_loaded));
                    textViewNoBudgets.setVisibility(View.GONE); // Masquer le texte "Aucun budget"
                    recyclerViewBudgets.setVisibility(View.VISIBLE); // Afficher le RecyclerView
                    if (budgetAdapter != null) {
                        budgetAdapter.setCurrency(currentCurrency); // Mettre à jour la devise
                        budgetAdapter.setBudgets(budgets); // Mettre à jour les budgets
                    }
                } else {
                    Log.w(getString(R.string.log_tag_budget_activity), getString(R.string.log_no_budgets_found));
                    showNoBudgetsMessage();
                }
            });
        });
    }

    public void onLastBudgetRemoved() {
        Log.d(getString(R.string.log_tag_budget_activity), getString(R.string.log_last_budget_removed));
        showNoBudgetsMessage();
    }

    public void showNoBudgetsMessage() {
        textViewNoBudgets.setVisibility(View.VISIBLE); // Afficher le message
        recyclerViewBudgets.setVisibility(View.GONE); // Masquer le RecyclerView
    }

}