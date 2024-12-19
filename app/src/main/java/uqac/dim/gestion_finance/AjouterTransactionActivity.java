package uqac.dim.gestion_finance;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Budget;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class AjouterTransactionActivity extends AppCompatActivity {

    private static final String TAG = "AjouterTransactionActivity";

    private EditText editTextTransactionName, editTextTransactionAmount, editTextNotes;
    private Spinner spinnerBudget;
    private Button buttonSelectDate, buttonSaveTransaction;

    private AppDatabase db;
    private String selectedDate = "";
    private final HashMap<String, Integer> budgetIdMap = new HashMap<>(); // Map pour stocker nom-budget -> id-budget

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_transaction);

        // Configurer la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_transaction);
        }

        // Initialiser les vues
        initializeViews();

        // Charger la base de données
        db = AppDatabase.getDatabase(getApplicationContext());

        // Charger les budgets dynamiques
        loadBudgets();

        // Configurer le bouton de sélection de date
        setupDateButton();

        // Configurer le bouton de sauvegarde
        setupSaveButton();
    }

    private void initializeViews() {
        editTextTransactionName = findViewById(R.id.editTextTransactionName);
        editTextTransactionAmount = findViewById(R.id.editTextTransactionAmount);
        editTextNotes = findViewById(R.id.editTextNotes);
        spinnerBudget = findViewById(R.id.spinnerBudget);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSaveTransaction = findViewById(R.id.buttonSaveTransaction);
    }

    private void loadBudgets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Budget> budgets = db.budgetDao().getAllBudgets(); // Charger les budgets depuis la base de données
            List<String> budgetNames = new ArrayList<>();
            budgetIdMap.clear();

            for (Budget budget : budgets) {
                budgetNames.add(budget.nom);
                budgetIdMap.put(budget.nom, budget.id); // Associer le nom au budget ID
            }

            runOnUiThread(() -> {
                if (!budgetNames.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, budgetNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerBudget.setAdapter(adapter);
                } else {
                    showErrorDialog(getString(R.string.error_no_budgets_available));
                }
            });
        });
    }

    private void setupDateButton() {
        buttonSelectDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        selectedDate = sdf.format(calendar.getTime());
                        buttonSelectDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });
    }

    private void setupSaveButton() {
        buttonSaveTransaction.setOnClickListener(v -> {
            String transactionName = editTextTransactionName.getText().toString().trim();
            String amountStr = editTextTransactionAmount.getText().toString().trim();
            String notes = editTextNotes.getText().toString().trim();
            String selectedBudget = spinnerBudget.getSelectedItem() != null ? spinnerBudget.getSelectedItem().toString() : "";

            if (transactionName.isEmpty() || amountStr.isEmpty() || selectedBudget.isEmpty() || selectedDate.isEmpty()) {
                showErrorDialog(getString(R.string.error_fill_all_fields));
                return;
            }

            if (!validateAmount(amountStr)) return;

            double amount = Double.parseDouble(amountStr);
            int budgetId = budgetIdMap.getOrDefault(selectedBudget, -1);

            if (budgetId == -1) {
                showErrorDialog(getString(R.string.error_invalid_budget));
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                // Vérifier si le budget est actif
                boolean isActive = db.budgetDao().isBudgetActive(budgetId);

                runOnUiThread(() -> {
                    if (!isActive) {
                        showErrorDialog(getString(R.string.error_budget_inactive));
                    } else {
                        // Insérer la transaction si le budget est actif
                        insertTransaction(transactionName, amount, notes, budgetId);
                    }
                });
            });
        });
    }

    private void insertTransaction(String name, double amount, String notes, int budgetId) {
        int id = getLastUserId();
        Executors.newSingleThreadExecutor().execute(() -> {
            UserTransaction transaction = new UserTransaction();
            transaction.Nom_transaction = name;
            transaction.Montant = amount;
            transaction.Date_transaction = selectedDate;
            transaction.ID_Utilisateur = id; // Exemple d'utilisateur
            transaction.ID_Categorie = budgetId;
            transaction.ID_Mode = 0;
            transaction.Recurrence = false;

            db.transactionDao().insert(transaction);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.transaction_saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private int getLastUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
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