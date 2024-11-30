package uqac.dim.gestion_finance;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Appliquer les paramètres globaux (langue, mode sombre, etc.)
        GlobalSettings globalSettings = new GlobalSettings(this);
        globalSettings.applyGlobalSettings();
    }

    @Override
    protected void attachBaseContext(Context base) {
        GlobalSettings globalSettings = new GlobalSettings(base);
        Context localizedContext = LocaleHelper.wrap(base, globalSettings.getLanguage());
        super.attachBaseContext(localizedContext);
    }
}