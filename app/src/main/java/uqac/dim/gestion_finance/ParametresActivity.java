package uqac.dim.gestion_finance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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

        // Configurer le switch de mode sombre
        configureDarkModeSwitch(globalSettings);

        // Charger les paramètres actuels
        loadCurrentSettings(globalSettings);

        // Configurer le bouton de sauvegarde
        buttonSaveSettings.setOnClickListener(v -> saveSettings(globalSettings));

        // Configurer le bouton de déconnexion
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
        if (currentLanguage.equals(getString(R.string.language_code_fr))) {
            spinnerLanguage.setSelection(0);
        } else {
            spinnerLanguage.setSelection(1);
        }

        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);
    }

    private void configureDarkModeSwitch(GlobalSettings globalSettings) {
        // Synchroniser le switch avec GlobalSettings
        boolean isDarkModeEnabled = globalSettings.isDarkModeEnabled();
        switchDarkMode.setChecked(isDarkModeEnabled);

        // Ajouter un écouteur pour les changements
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            globalSettings.setDarkModeEnabled(isChecked);
            globalSettings.applyGlobalSettings();
        });
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

        // Sauvegarde des paramètres dans GlobalSettings
        globalSettings.setLanguage(languageCode);
        globalSettings.setDarkModeEnabled(isDarkModeEnabled);
        globalSettings.setCurrency(selectedCurrency);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Préparez l'objet Parametres
                Parametres parametres = new Parametres();
                parametres.ID_Utilisateur = currentUserId;
                parametres.Langue = languageCode;
                parametres.Devise = selectedCurrency;
                parametres.Notifications = isNotificationsEnabled;
                parametres.Mode_sombre = isDarkModeEnabled;

                // Vérifiez et insérez/mettre à jour les paramètres
                Parametres existingParams = parametresDao.getByUserId(currentUserId);

                if (existingParams != null) {
                    // Mettre à jour les paramètres
                    parametres.ID_Parametres = existingParams.ID_Parametres;
                    parametresDao.update(parametres);

                } else {
                    // Insérer les nouveaux paramètres
                    parametresDao.insert(parametres);
                }
                // Appliquer les changements d'UI
                runOnUiThread(() -> {
                    LocaleHelper.setLocale(this, globalSettings.getLanguage());
                    updateUI(globalSettings); // Met à jour les éléments de l'interface utilisateur
                });
            } catch (Exception e) {
            }
        });
    }

    private void restartActivity() {
        Intent intent = getIntent(); // Récupérer l'intent actuel
        finish(); // Terminer l'activité actuelle
        overridePendingTransition(0, 0); // Désactiver les animations de transition
        startActivity(intent); // Relancer l'activité
        overridePendingTransition(0, 0); // Désactiver les animations de transition
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
                                getString(R.string.account_info, user.Nom, user.Email)
                        );
                    } else {
                        textViewAccountInfo.setText(getString(R.string.informations_non_disponibles));
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.error_fetching_account_info), e);
            }
        });
    }

    private void updateAppInfo() {
        String appVersion = getString(R.string.app_version);
        String developers = getString(R.string.developers_name);

        textViewAppInfo.setText(
                getString(R.string.application_info, appVersion, developers)
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

    private void updateUI(GlobalSettings globalSettings) {
        // Mettre à jour la langue globalement avant la mise à jour des vues
        LocaleHelper.setLocale(this, globalSettings.getLanguage());

        // Mise à jour de la langue sélectionnée dans le spinner
        String currentLanguage = globalSettings.getLanguage();
        spinnerLanguage.setSelection(currentLanguage.equals(getString(R.string.default_language)) ? 0 : 1);

        // Mise à jour de la devise sélectionnée dans le spinner
        String currentCurrency = globalSettings.getCurrency();
        spinnerCurrency.setSelection(getIndex(spinnerCurrency, currentCurrency));

        // Mise à jour de l'état du switch de mode sombre
        boolean isDarkModeEnabled = globalSettings.isDarkModeEnabled();
        switchDarkMode.setChecked(isDarkModeEnabled);

        // Mise à jour des autres informations visibles (comme l'info de compte et app info)
        updateAccountInfo();
        updateAppInfo();

        // Recharger l'activité pour les changements de langue et de mode sombre
        restartActivity();
    }


}