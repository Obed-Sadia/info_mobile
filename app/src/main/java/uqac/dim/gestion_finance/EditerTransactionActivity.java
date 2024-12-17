package uqac.dim.gestion_finance;

import android.app.DatePickerDialog;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Budget;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class EditerTransactionActivity extends AppCompatActivity {

    private static final String TAG = "EditerTransactionActivity";

    private EditText editTextTransactionName, editTextTransactionAmount, editTextNotes;
    private Spinner spinnerBudget;
    private Button buttonSelectDate, buttonSaveTransaction;

    private AppDatabase db;
    private String selectedDate = "";
    private int transactionId; // ID de la transaction à modifier

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_transaction);

        // Configurer la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_transaction);
        }

        // Récupérer l'ID de la transaction
        transactionId = getIntent().getIntExtra("transactionId", -1);
        if (transactionId == -1) {
            Toast.makeText(this, "ID transaction invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initializeViews();

        // Charger la base de données
        db = AppDatabase.getDatabase(getApplicationContext());

        // Charger les budgets dynamiques
        loadBudgets();

        // Charger les détails de la transaction
        loadTransactionDetails();

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
            List<Budget> budgets = db.budgetDao().getAllBudgets();
            List<String> budgetNames = new ArrayList<>();
            for (Budget budget : budgets) {
                budgetNames.add(budget.nom);
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, budgetNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBudget.setAdapter(adapter);
            });
        });
    }

    private void loadTransactionDetails() {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserTransaction transaction = db.transactionDao().getById(transactionId);
            if (transaction != null) {
                runOnUiThread(() -> populateFields(transaction));
            } else {
                Log.e(TAG, "Transaction introuvable");
                finish();
            }
        });
    }

    private void populateFields(UserTransaction transaction) {
        editTextTransactionName.setText(transaction.Nom_transaction);
        editTextTransactionAmount.setText(String.valueOf(transaction.Montant));
        editTextNotes.setText(""); // Notes non stockées dans cette version

        selectedDate = transaction.Date_transaction;
        buttonSelectDate.setText(selectedDate);
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

            if (transactionName.isEmpty() || amountStr.isEmpty() || selectedDate.isEmpty()) {
                showErrorDialog(getString(R.string.error_fill_all_fields));
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showErrorDialog(getString(R.string.error_invalid_amount));
                return;
            }

            // Mettre à jour la transaction
            Executors.newSingleThreadExecutor().execute(() -> {
                UserTransaction transaction = new UserTransaction();
                transaction.ID_Transaction = transactionId;
                transaction.Nom_transaction = transactionName;
                transaction.Montant = amount;
                transaction.Date_transaction = selectedDate;

                db.transactionDao().update(transaction);

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.transaction_saved, Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
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