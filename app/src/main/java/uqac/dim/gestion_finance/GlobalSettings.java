package uqac.dim.gestion_finance;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.app.UiModeManager;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class GlobalSettings {

    private static final String PREFERENCES_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "LANGUAGE";
    private static final String KEY_DARK_MODE = "DARK_MODE";

    private final Context context;

    public GlobalSettings(Context context) {
        this.context = context;
    }

    public String getLanguage() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String language = preferences.getString(KEY_LANGUAGE, null);

        if (language == null) {
            // Récupérer la langue du téléphone
            String systemLanguage = Locale.getDefault().getLanguage();

            // Vérifier si la langue est supportée
            if (!systemLanguage.equals("fr") && !systemLanguage.equals("en")) {
                systemLanguage = "fr"; // Par défaut : Français
            }

            // Sauvegarder la langue détectée
            setLanguage(systemLanguage);

            return systemLanguage;
        }

        return language;
    }

    public void setLanguage(String languageCode) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, languageCode)
                .apply();
    }

    public boolean isDarkModeEnabled() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        // Vérifiez si le mode sombre a déjà été défini
        if (!preferences.contains(KEY_DARK_MODE)) {
            // Détectez l'état du mode sombre du système
            int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

            // Sauvegardez cet état par défaut
            setDarkModeEnabled(isSystemDarkMode);

            return isSystemDarkMode;
        }

        // Retournez la valeur sauvegardée
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkModeEnabled(boolean isEnabled) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_MODE, isEnabled)
                .apply();
    }

    public void clearUserSession() {
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .edit()
                .remove("USER_ID")
                .apply();
    }

    public int getUserId() {
        return context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getInt("USER_ID", -1);
    }

    /**
     * Détecte si le mode sombre est activé sur le téléphone.
     */
    private boolean isSystemDarkModeEnabled() {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Applique les paramètres globaux (langue et mode sombre) à l'ensemble de l'application.
     */
    public void applyGlobalSettings() {
        // Appliquer la langue
        LocaleHelper.setLocale(context, getLanguage());

        // Appliquer le mode sombre
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}