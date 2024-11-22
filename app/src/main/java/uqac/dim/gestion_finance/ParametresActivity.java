package uqac.dim.gestion_finance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.dao.ParametresDao;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Parametres;
import uqac.dim.gestion_finance.entities.Utilisateur;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParametresActivity extends AppCompatActivity {

    private Spinner spinnerLanguage, spinnerCurrency;
    private Switch switchNotifications, switchDarkMode;
    private TextView textViewAccountInfo, textViewAppInfo;
    private Button buttonSaveSettings;
    private AppDatabase db;
    private ParametresDao parametresDao;
    private int currentUserId;
    private static final String TAG = "ParametresActivity";

    private BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Récupérer la langue sauvegardée dans les préférences
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String language = prefs.getString("LANGUAGE", "fr"); // Par défaut : français

        // Appliquer la langue dès le démarrage
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        // Récupérer l'ID de l'utilisateur depuis les préférences partagées
        currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            // Gérer le cas où l'ID n'est pas trouvé
            Toast.makeText(this, getString(R.string.erreur_utilisateur_non_identifie), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialisation des vues
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        textViewAccountInfo = findViewById(R.id.textViewAccountInfo);
        textViewAppInfo = findViewById(R.id.textViewAppInfo);
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings);
        Button buttonLogout = findViewById(R.id.buttonLogout);


        // Initialisation de la base de données
        db = AppDatabase.getDatabase(getApplicationContext());
        parametresDao = db.parametresDao();

        // Configuration des spinners
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.language_french), getString(R.string.language_english)}
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);

        // Chargement des paramètres actuels
        loadCurrentSettings();

        // Configuration du bouton de sauvegarde
        buttonSaveSettings.setOnClickListener(v -> saveSettings());

        // Configuration du bouton de déconnexion
        buttonLogout.setOnClickListener(v -> logoutUser());

        // Affichage des informations sur le compte et l'application
        updateAccountInfo();
        updateAppInfo();
        initializeViews();
        setupNavigation();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    private void loadCurrentSettings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final Parametres parametres = parametresDao.getByUserId(currentUserId);
                runOnUiThread(() -> {
                    if (parametres != null) {
                        // Mapper le code de langue aux noms explicites dans le spinner
                        if (parametres.Langue.equals("fr")) {
                            spinnerLanguage.setSelection(getIndex(spinnerLanguage, getString(R.string.language_french)));
                        } else if (parametres.Langue.equals("en")) {
                            spinnerLanguage.setSelection(getIndex(spinnerLanguage, getString(R.string.language_english)));
                        }

                        spinnerCurrency.setSelection(getIndex(spinnerCurrency, parametres.Devise));
                        switchNotifications.setChecked(parametres.Notifications);
                        switchDarkMode.setChecked(parametres.Mode_sombre);
                    } else {
                        Toast.makeText(ParametresActivity.this, "Aucun paramètre trouvé pour cet utilisateur", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ParametresActivity.this, "Erreur lors du chargement des paramètres", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void saveSettings() {
        // Récupérer tous les paramètres
        String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
        String selectedCurrency = spinnerCurrency.getSelectedItem().toString();
        boolean isNotificationsEnabled = switchNotifications.isChecked();
        boolean isDarkModeEnabled = switchDarkMode.isChecked();

        // Mapper les langues explicites aux codes de langue
        String languageCode;
        if (selectedLanguage.equals(getString(R.string.language_french))) {
            languageCode = "fr";
        } else if (selectedLanguage.equals(getString(R.string.language_english))) {
            languageCode = "en";
        } else {
            languageCode = "fr"; // Par défaut
        }

        // Sauvegarder les paramètres dans la base de données
        Parametres parametres = new Parametres();
        parametres.ID_Utilisateur = currentUserId;
        parametres.Langue = languageCode;
        parametres.Devise = selectedCurrency;
        parametres.Notifications = isNotificationsEnabled;
        parametres.Mode_sombre = isDarkModeEnabled;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Parametres existingParams = parametresDao.getByUserId(currentUserId);
                if (existingParams != null) {
                    parametres.ID_Parametres = existingParams.ID_Parametres;
                    parametresDao.update(parametres);
                } else {
                    parametresDao.insert(parametres);
                }

                // Sauvegarder les paramètres globalement
                SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("LANGUAGE", languageCode);
                editor.putString("CURRENCY", selectedCurrency);
                editor.putBoolean("NOTIFICATIONS", isNotificationsEnabled);
                editor.putBoolean("DARK_MODE", isDarkModeEnabled);
                editor.apply();

                runOnUiThread(() -> {
                    Toast.makeText(
                            ParametresActivity.this,
                            getString(R.string.parametres_sauvegardes),
                            Toast.LENGTH_SHORT
                    ).show();

                    // Appliquer les paramètres et redémarrer l'activité
                    applyGlobalSettings(parametres);
                    Log.d(TAG, "applyGlobalSettings called with language: " + parametres.Langue);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(
                        ParametresActivity.this,
                        getString(R.string.erreur_sauvegarde_parametres),
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void applyGlobalSettings(Parametres parametres) {
        // Appliquer la langue
        LocaleHelper.setLocale(this, parametres.Langue);

        // Appliquer le mode sombre
        if (parametres.Mode_sombre) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Redémarrer l'activité pour appliquer tous les changements
        overridePendingTransition(0, 0); // Désactiver les animations
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0); // Désactiver les animations
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
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(
                        ParametresActivity.this,
                        getString(R.string.erreur_recuperation_compte),
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void updateAppInfo() {
        String appVersion = getString(R.string.app_version); // Version définie dans strings.xml
        String developers = getString(R.string.developers_name); // Référence depuis strings.xml

        textViewAppInfo.setText(
                getString(R.string.version_application, appVersion, developers)
        );
    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation == null) {
            Log.e(TAG, "initializeViews: BottomNavigation is null");
            Toast.makeText(this, "Erreur lors de l'initialisation de l'interface", Toast.LENGTH_LONG).show();
            finish();
        }

        // Sélectionner l'élément correspondant à BudgetActivity
        bottomNavigation.setSelectedItemId(R.id.navigation_parametres);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(ParametresActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish();
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                Intent intent = new Intent(ParametresActivity.this, TransactionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish();
                return true;
            } else if (itemId == R.id.navigation_budget) {
                Intent intent = new Intent(ParametresActivity.this, BudgetActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Désactiver les animations
                finish();
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                return true;
            }
            return false;
        });
    }

    private void logoutUser() {
        // Supprimez l'ID de l'utilisateur des SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("USER_ID"); // Supprime l'ID utilisateur
        editor.apply();

        // Redirection vers l'écran de connexion
        Intent intent = new Intent(ParametresActivity.this, ConnexionActivity.class);
        startActivity(intent);

        // Fermer toutes les activités existantes
        finishAffinity();
    }
}