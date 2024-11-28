package uqac.dim.gestion_finance;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AjouterBudgetActivity extends AppCompatActivity {

    private EditText editTextBudgetName, editTextAmount;
    private Spinner spinnerCategories, spinnerTemporality, spinnerDays;
    private Button buttonResetBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_budget);

        // Configurer la Toolbar avec flèche Précédent
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_budget);
        }

        // Initialiser les vues
        initializeViews();

        // Configurer le bouton Réinitialiser
        setupResetButton();
    }

    // Initialiser les vues
    private void initializeViews() {
        editTextBudgetName = findViewById(R.id.editTextBudgetName);
        editTextAmount = findViewById(R.id.editTextAmount);
        spinnerCategories = findViewById(R.id.spinnerCategories);
        spinnerTemporality = findViewById(R.id.spinnerTemporality);
        spinnerDays = findViewById(R.id.spinnerDays);
        buttonResetBudget = findViewById(R.id.buttonResetBudget);
    }

    // Configurer le bouton Réinitialiser
    private void setupResetButton() {
        buttonResetBudget.setOnClickListener(v -> {
            // Réinitialiser les champs de texte
            editTextBudgetName.setText("");
            editTextAmount.setText("");

            // Réinitialiser les spinners à leur première position
            spinnerCategories.setSelection(0);
            spinnerTemporality.setSelection(0);
            spinnerDays.setSelection(0);

            // Masquer les champs liés aux jours si la temporalité n'est pas mensuelle
            spinnerDays.setVisibility(spinnerTemporality.getSelectedItemPosition() == 1 ? Spinner.VISIBLE : Spinner.GONE);
        });
    }

    // Gérer la flèche Précédent
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Terminer l'activité actuelle
        return true;
    }
}