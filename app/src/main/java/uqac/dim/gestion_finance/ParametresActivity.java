package uqac.dim.gestion_finance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
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

public class ParametresActivity extends AppCompatActivity {

    private Spinner spinnerLanguage, spinnerCurrency;
    private Switch switchNotifications, switchDarkMode;
    private TextView textViewAccountInfo, textViewAppInfo;
    private Button buttonSaveSettings;
    private AppDatabase db;
    private ParametresDao parametresDao;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        // Récupérer l'ID de l'utilisateur depuis les préférences partagées
        currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            // Gérer le cas où l'ID n'est pas trouvé
            Toast.makeText(this, "Erreur: Utilisateur non identifié", Toast.LENGTH_LONG).show();
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

        // Initialisation de la base de données
        db = AppDatabase.getDatabase(getApplicationContext());
        parametresDao = db.parametresDao();

        // Configuration des spinners
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);

        // Chargement des paramètres actuels
        loadCurrentSettings();

        // Configuration du bouton de sauvegarde
        buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        // Affichage des informations sur le compte et l'application
        updateAccountInfo();
        updateAppInfo();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    private void loadCurrentSettings() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Parametres parametres = parametresDao.getByUserId(currentUserId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (parametres != null) {
                                spinnerLanguage.setSelection(getIndex(spinnerLanguage, parametres.Langue));
                                spinnerCurrency.setSelection(getIndex(spinnerCurrency, parametres.Devise));
                                switchNotifications.setChecked(parametres.Notifications);
                                switchDarkMode.setChecked(parametres.Mode_sombre);
                            } else {
                                // Gérer le cas où aucun paramètre n'est trouvé
                                Toast.makeText(ParametresActivity.this, "Aucun paramètre trouvé pour cet utilisateur", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ParametresActivity.this, "Erreur lors du chargement des paramètres", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void saveSettings() {
        Parametres parametres = new Parametres();
        parametres.ID_Utilisateur = currentUserId;
        parametres.Langue = spinnerLanguage.getSelectedItem().toString();
        parametres.Devise = spinnerCurrency.getSelectedItem().toString();
        parametres.Notifications = switchNotifications.isChecked();
        parametres.Mode_sombre = switchDarkMode.isChecked();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Parametres existingParams = parametresDao.getByUserId(currentUserId);
                    if (existingParams != null) {
                        parametres.ID_Parametres = existingParams.ID_Parametres;
                        parametresDao.update(parametres);
                    } else {
                        parametresDao.insert(parametres);
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(ParametresActivity.this, "Paramètres sauvegardés", Toast.LENGTH_SHORT).show();
                        applyGlobalSettings(parametres);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ParametresActivity.this, "Erreur lors de la sauvegarde des paramètres", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void applyGlobalSettings(Parametres parametres) {
        // Appliquer le mode sombre
        if (parametres.Mode_sombre) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Appliquer la langue
        Locale newLocale = new Locale(parametres.Langue);
        Locale.setDefault(newLocale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(newLocale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Sauvegarder la devise pour une utilisation globale
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("CURRENCY", parametres.Devise);
        editor.apply();

        // Notifier les autres activités du changement de paramètres
        Intent intent = new Intent("SETTINGS_UPDATED");
        sendBroadcast(intent);

        // Redémarrer l'activité principale pour appliquer les changements
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }



    private void updateAccountInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final Utilisateur user = db.utilisateurDao().getById(currentUserId);
                runOnUiThread(() -> {
                    if (user != null) {
                        textViewAccountInfo.setText("Nom d'utilisateur: " + user.Nom + "\nEmail: " + user.Email);
                    } else {
                        textViewAccountInfo.setText("Informations non disponibles");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ParametresActivity.this, "Erreur lors de la récupération des informations du compte", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void updateAppInfo() {
        textViewAppInfo.setText("Version de l'application: 1.0\nDéveloppé par: La team Lion");
    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }
}