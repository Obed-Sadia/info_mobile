package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BudgetActivity extends AppCompatActivity {

    private static final String TAG = "BudgetActivity";

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // La configuration globale est déjà appliquée via MyApplication/GlobalSettings
        setContentView(R.layout.activity_budget);
        Log.d(TAG, "onCreate: BudgetActivity started");

        initializeViews();
        setupNavigation();
        setupFab();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAddBudget = findViewById(R.id.fabAddBudget);

        if (bottomNavigation == null) {
            Log.e(TAG, "initializeViews: BottomNavigation is null");
            finish();
        }

        // Sélectionner l'élément correspondant à BudgetActivity
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
            Log.d(TAG, "setupFab: FloatingActionButton clicked");

            // Remplacez l'action ici par ce que vous voulez faire (exemple : ouvrir une nouvelle activité)
            Log.d(TAG, "setupFab: Redirection vers AjouterBudgetActivity");

            // Exemple : Redirection vers une activité de création de budget
            Intent intent = new Intent(BudgetActivity.this, AjouterBudgetActivity.class);
            startActivity(intent);
        });
    }
}