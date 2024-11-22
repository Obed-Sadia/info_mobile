package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.dao.ParametresDao;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Parametres;
import uqac.dim.gestion_finance.entities.Utilisateur;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParametresActivity extends AppCompatActivity {

    private static final String TAG = "ParametresActivity";

    private Spinner spinnerLanguage, spinnerCurrency;
    private Switch switchNotifications, switchDarkMode;
    private TextView textViewAccountInfo, textViewAppInfo;
    private Button buttonSaveSettings;
    private AppDatabase db;
    private ParametresDao parametresDao;
    private int currentUserId;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        GlobalSettings globalSettings = new GlobalSettings(this);

        // Récupérer l'ID utilisateur
        currentUserId = globalSettings.getUserId();
        if (currentUserId == -1) {
            Log.e(TAG, "Utilisateur non identifié.");
            finish();
            return;
        }

        // Initialiser les vues
        initializeViews();

        // Initialiser la base de données
        db = AppDatabase.getDatabase(getApplicationContext());
        parametresDao = db.parametresDao();

        // Configurer les spinners
        configureSpinners(globalSettings);

        // Charger les paramètres actuels
        loadCurrentSettings(globalSettings);

        // Sauvegarder les modifications
        buttonSaveSettings.setOnClickListener(v -> saveSettings(globalSettings));

        // Déconnexion
        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> logoutUser(globalSettings));

        // Configurer la navigation
        setupNavigation();
    }

    private void initializeViews() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        textViewAccountInfo = findViewById(R.id.textViewAccountInfo);
        textViewAppInfo = findViewById(R.id.textViewAppInfo);
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_parametres);
        }
    }

    private void configureSpinners(GlobalSettings globalSettings) {
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.language_french), getString(R.string.language_english)}
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        // Définir la langue sélectionnée
        String currentLanguage = globalSettings.getLanguage();
        if (currentLanguage.equals("fr")) {
            spinnerLanguage.setSelection(0);
        } else {
            spinnerLanguage.setSelection(1);
        }

        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);
    }

    private void loadCurrentSettings(GlobalSettings globalSettings) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final Parametres parametres = parametresDao.getByUserId(currentUserId);
                runOnUiThread(() -> {
                    if (parametres != null) {
                        spinnerCurrency.setSelection(getIndex(spinnerCurrency, parametres.Devise));
                        switchNotifications.setChecked(parametres.Notifications);
                        switchDarkMode.setChecked(globalSettings.isDarkModeEnabled());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du chargement des paramètres", e);
            }
        });

        // Mettre à jour les informations du compte et de l'application
        updateAccountInfo();
        updateAppInfo();
    }

    private void saveSettings(GlobalSettings globalSettings) {
        String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
        String selectedCurrency = spinnerCurrency.getSelectedItem().toString();
        boolean isNotificationsEnabled = switchNotifications.isChecked();
        boolean isDarkModeEnabled = switchDarkMode.isChecked();

        String languageCode = selectedLanguage.equals(getString(R.string.language_french)) ? "fr" : "en";

        globalSettings.setLanguage(languageCode);
        globalSettings.setDarkModeEnabled(isDarkModeEnabled);
        globalSettings.applyGlobalSettings();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Parametres parametres = new Parametres();
                parametres.ID_Utilisateur = currentUserId;
                parametres.Langue = languageCode;
                parametres.Devise = selectedCurrency;
                parametres.Notifications = isNotificationsEnabled;
                parametres.Mode_sombre = isDarkModeEnabled;

                Parametres existingParams = parametresDao.getByUserId(currentUserId);
                if (existingParams != null) {
                    parametres.ID_Parametres = existingParams.ID_Parametres;
                    parametresDao.update(parametres);
                } else {
                    parametresDao.insert(parametres);
                }

                runOnUiThread(() -> {
                    Log.d(TAG, "Paramètres sauvegardés et appliqués");

                    // Redémarrage explicite de l'activité pour appliquer les changements immédiatement
                    restartActivity();
                });
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la sauvegarde des paramètres", e);
            }
        });
    }

    /**
     * Redémarre l'activité actuelle pour refléter les changements.
     */
    private void restartActivity() {
        Intent intent = getIntent(); // Récupérer l'intent actuel
        finish(); // Terminer l'activité actuelle
        startActivity(intent); // Relancer l'activité
        overridePendingTransition(0, 0); // Désactiver les animations
    }

    private void logoutUser(GlobalSettings globalSettings) {
        globalSettings.clearUserSession();

        Intent intent = new Intent(ParametresActivity.this, ConnexionActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    private void updateAccountInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final Utilisateur user = db.utilisateurDao().getById(currentUserId);
                runOnUiThread(() -> {
                    if (user != null) {
                        textViewAccountInfo.setText(
                                getString(R.string.nom_utilisateur, user.Nom, user.Email)
                        );
                    } else {
                        textViewAccountInfo.setText(getString(R.string.informations_non_disponibles));
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la récupération des informations de compte", e);
            }
        });
    }

    private void updateAppInfo() {
        String appVersion = getString(R.string.app_version);
        String developers = getString(R.string.developers_name);

        textViewAppInfo.setText(
                getString(R.string.version_application, appVersion, developers)
        );
    }

    private void setupNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    Intent intent = new Intent(ParametresActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_transaction) {
                    Intent intent = new Intent(ParametresActivity.this, TransactionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    Intent intent = new Intent(ParametresActivity.this, BudgetActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_parametres) {
                    return true;
                }
                return false;
            });
        }
    }
}