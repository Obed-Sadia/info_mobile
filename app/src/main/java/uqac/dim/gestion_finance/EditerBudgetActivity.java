package uqac.dim.gestion_finance;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.Executors;
import java.text.DecimalFormat;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Budget;

public class EditerBudgetActivity extends AppCompatActivity {

    private static final String TAG = "EditerBudgetActivity";

    private EditText editTextBudgetName, editTextAmount;
    private AutoCompleteTextView autoCompleteCategory;
    private Spinner spinnerTemporality;
    private Button buttonUpdateBudget;
    private AppDatabase db;
    private int budgetId; // ID du budget à modifier

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editer_budget);

        // Configurer la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_budget);
        }

        // Initialiser les vues
        initializeViews();

        // Charger la base de données
        db = AppDatabase.getDatabase(getApplicationContext());

        // Récupérer l'ID du budget à modifier
        budgetId = getIntent().getIntExtra("budgetId", -1);
        if (budgetId == -1) {
            Log.e(TAG, "Aucun ID de budget fourni !");
            finish();
            return;
        }

        // Charger les données du budget
        loadBudgetData();

        // Configurer le bouton de mise à jour
        setupUpdateButton();
    }

    private void initializeViews() {
        editTextBudgetName = findViewById(R.id.editTextBudgetName);
        editTextAmount = findViewById(R.id.editTextAmount);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        spinnerTemporality = findViewById(R.id.spinnerTemporality);
        buttonUpdateBudget = findViewById(R.id.buttonUpdateBudget);

        // Configurer les options de récurrence
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.recurrence_options));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemporality.setAdapter(adapter);
    }

    private void loadBudgetData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Budget budget = db.budgetDao().getBudgetById(budgetId);
            if (budget != null) {
                runOnUiThread(() -> populateFields(budget));
            } else {
                Log.e(TAG, "Budget introuvable !");
                finish();
            }
        });
    }

    private void populateFields(Budget budget) {
        editTextBudgetName.setText(budget.nom);

        // Formater le montant avec deux décimales
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        editTextAmount.setText(decimalFormat.format(budget.montant));

        autoCompleteCategory.setText(budget.categorie);

        // Définir la récurrence par défaut dans le spinner
        String[] recurrenceOptions = getResources().getStringArray(R.array.recurrence_options);
        String recurrenceLabel = ""; // Variable pour stocker la valeur à afficher dans le spinner

        switch (budget.recurrence) {
            case Budget.RECURRENCE_NONE:
                recurrenceLabel = getString(R.string.recurrence_none);
                break;
            case Budget.RECURRENCE_MONTHLY:
                recurrenceLabel = getString(R.string.recurrence_monthly);
                break;
            case Budget.RECURRENCE_YEARLY:
                recurrenceLabel = getString(R.string.recurrence_yearly);
                break;
        }

        // Trouver l'index de la récurrence correspondante dans le tableau des options
        int defaultIndex = getRecurrenceIndex(recurrenceOptions, recurrenceLabel);
        spinnerTemporality.setSelection(defaultIndex);
    }

    private int getRecurrenceIndex(String[] options, String recurrenceLabel) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(recurrenceLabel)) {
                return i;
            }
        }
        return 0; // Retourne "Aucune" par défaut si non trouvé
    }

    private void setupUpdateButton() {
        buttonUpdateBudget.setOnClickListener(v -> {
            String budgetName = editTextBudgetName.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            String category = autoCompleteCategory.getText().toString().trim();
            String recurrence = spinnerTemporality.getSelectedItem().toString();

            // Conversion de la récurrence affichée en une valeur interne
            String internalRecurrence;
            if (recurrence.equals(getString(R.string.recurrence_none))) {
                internalRecurrence = Budget.RECURRENCE_NONE;
            } else if (recurrence.equals(getString(R.string.recurrence_monthly))) {
                internalRecurrence = Budget.RECURRENCE_MONTHLY;
            } else if (recurrence.equals(getString(R.string.recurrence_yearly))) {
                internalRecurrence = Budget.RECURRENCE_YEARLY;
            } else {
                internalRecurrence = Budget.RECURRENCE_NONE; // Par défaut
            }

            if (budgetName.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                showErrorDialog(getString(R.string.error_fill_all_fields));
                return;
            }

            if (!validateAmount(amountStr)) return;

            double amount = Double.parseDouble(amountStr);

            Executors.newSingleThreadExecutor().execute(() -> {
                Budget budget = new Budget(budgetName, amount, category, internalRecurrence, true);
                budget.id = budgetId; // Assurez-vous que l'ID est correct pour la mise à jour
                db.budgetDao().update(budget);

                runOnUiThread(() -> {
                    Log.d(TAG, "Budget mis à jour avec succès : " + budget.nom);
                    finish(); // Retour à l'activité précédente
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}