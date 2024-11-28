package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AjouterTransactionActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_transaction);

        // Configurer la Toolbar avec flèche Précédent
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_transaction); // Optionnel : enlever le titre de la Toolbar
        }

    }

    // Gérer la flèche Précédent
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Terminer l'activité actuelle
        return true;
    }
}