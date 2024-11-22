package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "TransactionActivity";

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        Log.d(TAG, "onCreate: TransactionActivity started");

        initializeViews();
        setupNavigation();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

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
}