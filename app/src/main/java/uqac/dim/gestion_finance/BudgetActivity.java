package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BudgetActivity extends AppCompatActivity {

    private static final String TAG = "BudgetActivity";

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        Log.d(TAG, "onCreate: BudgetActivity started");

        initializeViews();
        setupNavigation();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation == null) {
            Log.e(TAG, "initializeViews: BottomNavigation is null");
            Toast.makeText(this, "Erreur lors de l'initialisation de l'interface", Toast.LENGTH_LONG).show();
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
}