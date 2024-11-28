package uqac.dim.gestion_finance;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Categorie;

public class AjouterBudgetActivity extends AppCompatActivity {

    private static final String TAG = "AjouterBudgetActivity";

    private EditText editTextBudgetName, editTextAmount;
    private AutoCompleteTextView autoCompleteCategory;
    private Spinner spinnerTemporality, spinnerDays;
    private Button buttonResetBudget;
    private AppDatabase db;

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

        // Charger les catégories dynamiques
        db = AppDatabase.getDatabase(getApplicationContext());
        loadCategories();

        // Configurer le bouton Réinitialiser
        setupResetButton();

        // Configurer le comportement de l'AutoCompleteTextView
        setupAutoCompleteCategory();
    }

    // Initialiser les vues
    private void initializeViews() {
        editTextBudgetName = findViewById(R.id.editTextBudgetName);
        editTextAmount = findViewById(R.id.editTextAmount);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory); // Remplace spinnerCategories
        spinnerTemporality = findViewById(R.id.spinnerTemporality);
        spinnerDays = findViewById(R.id.spinnerDays);
        buttonResetBudget = findViewById(R.id.buttonResetBudget);
    }

    // Configurer le bouton Réinitialiser
    private void setupResetButton() {
        buttonResetBudget.setOnClickListener(v -> {
            Log.d(TAG, "setupResetButton: Réinitialisation des champs.");

            // Réinitialiser les champs de texte
            editTextBudgetName.setText("");
            editTextAmount.setText("");

            // Réinitialiser l'AutoCompleteTextView
            autoCompleteCategory.setText("");

            // Réinitialiser les spinners à leur première position
            spinnerTemporality.setSelection(0);
            spinnerDays.setSelection(0);

            // Masquer les champs liés aux jours si la temporalité n'est pas mensuelle
            spinnerDays.setVisibility(spinnerTemporality.getSelectedItemPosition() == 1 ? Spinner.VISIBLE : Spinner.GONE);
        });
    }

    // Charger les catégories dynamiques
    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Categorie> categories = db.categorieDao().getAllCategories();
                List<String> categoryNames = new ArrayList<>();

                // Transformer les objets en noms
                for (Categorie categorie : categories) {
                    categoryNames.add(categorie.nom);
                }

                runOnUiThread(() -> {
                    if (!categoryNames.isEmpty()) {
                        Log.d(TAG, "loadCategories: Catégories chargées avec succès.");
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_dropdown_item_1line, categoryNames);
                        autoCompleteCategory.setAdapter(adapter); // Définir l'adaptateur pour AutoCompleteTextView
                    } else {
                        Log.w(TAG, "loadCategories: Aucune catégorie disponible dans la base de données.");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "loadCategories: Erreur lors du chargement des catégories.", e);
            }
        });
    }

    // Configurer le comportement de l'AutoCompleteTextView
    private void setupAutoCompleteCategory() {
        autoCompleteCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteCategory.showDropDown(); // Forcer l'affichage des suggestions
            }
        });

        autoCompleteCategory.setOnClickListener(v -> autoCompleteCategory.showDropDown());
    }

    // Gérer la flèche Précédent
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Terminer l'activité actuelle
        return true;
    }
}