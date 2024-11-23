package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AjouterBudgetActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_budget);

        // Configurer la Toolbar avec flèche Précédent
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Optionnel : enlever le titre de la Toolbar
        }

        // Initialiser la barre de navigation
        initializeNavigation();
    }

    private void initializeNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_budget);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(AjouterBudgetActivity.this, MainActivity.class));
                    finish();
                } else if (itemId == R.id.navigation_transaction) {
                    startActivity(new Intent(AjouterBudgetActivity.this, TransactionActivity.class));
                    finish();
                } else if (itemId == R.id.navigation_parametres) {
                    startActivity(new Intent(AjouterBudgetActivity.this, ParametresActivity.class));
                    finish();
                }
                return true;
            });
        }
    }

    // Gérer la flèche Précédent
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Terminer l'activité actuelle
        return true;
    }
}