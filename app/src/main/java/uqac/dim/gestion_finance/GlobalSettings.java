package uqac.dim.gestion_finance;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class GlobalSettings {

    private static final String PREFERENCES_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "LANGUAGE";
    private static final String KEY_DARK_MODE = "DARK_MODE";
    private static final String KEY_CURRENCY = "CURRENCY"; // Ajout de la clé pour la devise

    private final Context context;

    public GlobalSettings(Context context) {
        this.context = context;
    }

    /**
     * Récupère la langue actuellement définie dans les préférences.
     * Si aucune langue n'est sauvegardée, définit et retourne la langue du téléphone.
     */
    public String getLanguage() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        // Si aucune langue n'est sauvegardée, utiliser la langue du téléphone
        if (!preferences.contains(KEY_LANGUAGE)) {
            String defaultLanguage = Locale.getDefault().getLanguage();

            // Si la langue n'est pas supportée, utiliser l'anglais par défaut
            if (!defaultLanguage.equals(context.getString(R.string.language_code_fr)) &&
                    !defaultLanguage.equals(context.getString(R.string.language_code_en))) {
                defaultLanguage = context.getString(R.string.language_code_en);
            }

            // Sauvegarder la langue par défaut pour les prochaines sessions
            setLanguage(defaultLanguage);
            return defaultLanguage;
        }

        // Retourne la langue sauvegardée
        return preferences.getString(KEY_LANGUAGE, context.getString(R.string.language_code_en)); // Par défaut : anglais
    }

    /**
     * Définit la langue de l'application et sauvegarde cette préférence.
     */
    public void setLanguage(String languageCode) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, languageCode)
                .apply();

        // Appliquer immédiatement la langue
        setAppLocale(languageCode);

        Log.d(context.getString(R.string.log_tag_global_settings), context.getString(R.string.language_saved_log, languageCode));
    }

    /**
     * Vérifie si le mode sombre est activé.
     */
    public boolean isDarkModeEnabled() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        // Si aucune préférence n'existe, détecter l'état du mode sombre du système
        if (!preferences.contains(KEY_DARK_MODE)) {
            int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

            // Sauvegarder l'état actuel comme par défaut
            setDarkModeEnabled(isSystemDarkMode);
            return isSystemDarkMode;
        }

        // Retourner la valeur sauvegardée
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Active ou désactive le mode sombre dans les préférences.
     */
    public void setDarkModeEnabled(boolean isEnabled) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_MODE, isEnabled)
                .apply();
    }

    /**
     * Applique les paramètres globaux (langue, mode sombre, etc.) à l'application.
     */
    public void applyGlobalSettings() {
        // Appliquer la langue
        setAppLocale(getLanguage());

        // Appliquer le mode sombre
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Définit la locale de l'application pour les ressources.
     */
    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    /**
     * Récupère la devise actuellement définie dans les préférences.
     */

    public String getCurrency() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_CURRENCY, context.getString(R.string.default_currency)); // Par défaut : EUR
    }

    /**
     * Définit la devise et sauvegarde cette préférence.
     */
    public void setCurrency(String currency) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CURRENCY, currency)
                .apply();
    }

    /**
     * Efface les informations de session utilisateur (par ex., lors de la déconnexion).
     */
    public void clearUserSession() {
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .edit()
                .remove("USER_ID")
                .apply();
    }

    /**
     * Récupère l'ID utilisateur depuis les préférences.
     */
    public int getUserId() {
        return context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getInt("USER_ID", -1);
    }

    /**
     * Sauvegarde l'ID utilisateur pour une utilisation ultérieure.
     */
    public void setUserId(int userId) {
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("USER_ID", userId)
                .apply();
    }
}