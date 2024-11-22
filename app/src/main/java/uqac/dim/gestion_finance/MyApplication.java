package uqac.dim.gestion_finance;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialiser GlobalSettings et appliquer les paramètres
        GlobalSettings globalSettings = new GlobalSettings(this);
        globalSettings.applyGlobalSettings();
    }
}