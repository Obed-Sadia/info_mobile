package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Budget;
import uqac.dim.gestion_finance.entities.Categorie;

public class AjouterBudgetActivity extends AppCompatActivity {

    private static final String TAG = "AjouterBudgetActivity";

    private EditText editTextBudgetName, editTextAmount;
    private AutoCompleteTextView autoCompleteCategory;
    private Spinner spinnerTemporality;
    private Button buttonResetBudget, buttonSaveBudget;
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

        // Configurer le bouton Sauvegarder
        setupSaveButton();
    }

    // Initialiser les vues
    private void initializeViews() {
        editTextBudgetName = findViewById(R.id.editTextBudgetName);
        editTextAmount = findViewById(R.id.editTextAmount);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        spinnerTemporality = findViewById(R.id.spinnerTemporality);
        buttonResetBudget = findViewById(R.id.buttonResetBudget);
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget);
    }

    // Configurer le bouton Réinitialiser
    private void setupResetButton() {
        buttonResetBudget.setOnClickListener(v -> {
            Log.d(TAG, getString(R.string.log_reset_fields));
            editTextBudgetName.setText("");
            editTextAmount.setText("");
            autoCompleteCategory.setText("");
            spinnerTemporality.setSelection(0);
        });
    }

    // Charger les catégories dynamiques
    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Categorie> categories = db.categorieDao().getAllCategories();
            List<String> categoryNames = new ArrayList<>();
            for (Categorie categorie : categories) {
                categoryNames.add(categorie.nom);
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
                autoCompleteCategory.setAdapter(adapter);
            });
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.recurrence_options));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemporality.setAdapter(adapter);
    }

    // Configurer le bouton Sauvegarder
    private void setupSaveButton() {
        buttonSaveBudget.setOnClickListener(v -> {
            String budgetName = editTextBudgetName.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            String category = autoCompleteCategory.getText().toString().trim();
            String recurrence = spinnerTemporality.getSelectedItem().toString();

            if (budgetName.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                showErrorDialog(getString(R.string.error_fill_all_fields));
                return;
            }

            if (!validateAmount(amountStr)) return;

            Executors.newSingleThreadExecutor().execute(() -> {
                // Vérifier si un budget avec le même nom existe
                if (db.budgetDao().getBudgetByName(budgetName) != null) {
                    runOnUiThread(() -> showErrorDialog(getString(R.string.error_budget_exists)));
                    return;
                }

                // Ajouter la catégorie si elle n'existe pas
                if (db.categorieDao().getCategorieByName(category) == null) {
                    db.categorieDao().insert(new Categorie(category));
                }

                // Sauvegarder le budget
                double amount = Double.parseDouble(amountStr);
                Budget budget = new Budget(budgetName, amount, category, recurrence, true);
                db.budgetDao().insert(budget);

                runOnUiThread(() -> {
                    Log.d(TAG, getString(R.string.log_budget_saved));
                    navigateBackToBudgetActivity();
                });
            });
        });
    }

    private boolean validateAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showErrorDialog(getString(R.string.error_invalid_amount));
                return false;
            }
            if (!amountStr.matches("^\\d{1,9}(\\.\\d{1,2})?$")) {
                showErrorDialog(getString(R.string.error_invalid_amount));
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorDialog(getString(R.string.error_invalid_amount));
            return false;
        }
        return true;
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    // Naviguer automatiquement vers BudgetActivity après sauvegarde
    private void navigateBackToBudgetActivity() {
        Intent intent = new Intent(AjouterBudgetActivity.this, BudgetActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}