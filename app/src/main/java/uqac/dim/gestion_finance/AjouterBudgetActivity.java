package uqac.dim.gestion_finance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
    private TextView textViewDays;
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

        // Configurer le spinner de récurrence
        setupRecurrenceSpinner();

        // Configurer le bouton Réinitialiser
        setupResetButton();

        // Configurer le comportement de l'AutoCompleteTextView
        setupAutoCompleteCategory();

        // Configurer la gestion de la récurrence
        handleRecurrenceSelection();
    }

    // Initialiser les vues
    private void initializeViews() {
        editTextBudgetName = findViewById(R.id.editTextBudgetName);
        editTextAmount = findViewById(R.id.editTextAmount);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        spinnerTemporality = findViewById(R.id.spinnerTemporality);
        spinnerDays = findViewById(R.id.spinnerDays);
        textViewDays = findViewById(R.id.textViewDays);
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
            textViewDays.setVisibility(View.GONE);
            spinnerDays.setVisibility(View.GONE);
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
                        autoCompleteCategory.setAdapter(adapter);
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
                autoCompleteCategory.showDropDown();
            }
        });

        autoCompleteCategory.setOnClickListener(v -> autoCompleteCategory.showDropDown());
    }

    // Configurer le spinner de récurrence
    private void setupRecurrenceSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Aucune", "Mensuelle", "Annuelle"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemporality.setAdapter(adapter);
    }

    // Gérer la sélection du spinner de récurrence
    private void handleRecurrenceSelection() {
        spinnerTemporality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if ("Mensuelle".equals(selectedItem)) {
                    // Afficher le titre et le spinner des jours de fin
                    textViewDays.setVisibility(View.VISIBLE);
                    spinnerDays.setVisibility(View.VISIBLE);

                    // Charger les jours du mois depuis l'array XML
                    ArrayAdapter<CharSequence> daysAdapter = ArrayAdapter.createFromResource(
                            AjouterBudgetActivity.this,
                            R.array.days,
                            android.R.layout.simple_spinner_item
                    );
                    daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDays.setAdapter(daysAdapter);
                } else {
                    // Cacher le titre et le spinner des jours de fin
                    textViewDays.setVisibility(View.GONE);
                    spinnerDays.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Cacher le titre et le spinner des jours de fin par défaut
                textViewDays.setVisibility(View.GONE);
                spinnerDays.setVisibility(View.GONE);
            }
        });
    }

    // Gérer la flèche Précédent
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}