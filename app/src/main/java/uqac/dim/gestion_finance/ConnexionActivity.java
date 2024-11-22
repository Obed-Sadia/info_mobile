package uqac.dim.gestion_finance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.SessionToken;
import uqac.dim.gestion_finance.entities.Utilisateur;

public class ConnexionActivity extends AppCompatActivity {

    private static final long TOKEN_VALIDITY_DURATION = 30 * 1000; // 30 secondes par défaut (modifiable via une variable globale ou config)

    private EditText editTextEmailLogin, editTextMotDePasseLogin;
    private Button buttonConnexion;
    private TextView pageInscription;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Récupérer la langue sauvegardée dans les préférences
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String language = prefs.getString("LANGUAGE", getString(R.string.default_language)); // Par défaut : français
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        // Initialisation des vues
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextMotDePasseLogin = findViewById(R.id.editTextMotDePasseLogin);
        buttonConnexion = findViewById(R.id.buttonConnexion);
        pageInscription = findViewById(R.id.creationC0mpte);

        // Initialisation de la base de données
        db = AppDatabase.getDatabase(getApplicationContext());

        // Gestion de la connexion
        buttonConnexion.setOnClickListener(v -> {
            String loginInput = editTextEmailLogin.getText().toString().trim();
            String motDePasse = editTextMotDePasseLogin.getText().toString().trim();

            // Vérifiez que les champs ne sont pas vides
            if (loginInput.isEmpty() || motDePasse.isEmpty()) {
                Toast.makeText(ConnexionActivity.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifiez si l'utilisateur existe dans la base de données
            Executors.newSingleThreadExecutor().execute(() -> {
                Utilisateur utilisateur = null;

                // Vérifie si loginInput est un email ou un nom d'utilisateur
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(loginInput).matches()) {
                    utilisateur = db.utilisateurDao().getByEmailAndPassword(loginInput, motDePasse);
                } else {
                    utilisateur = db.utilisateurDao().getByUsernameAndPassword(loginInput, motDePasse);
                }

                final Utilisateur finalUtilisateur = utilisateur;
                runOnUiThread(() -> {
                    if (finalUtilisateur != null) {
                        onSuccessfulLogin(finalUtilisateur.ID_Utilisateur);
                    } else {
                        Toast.makeText(ConnexionActivity.this, getString(R.string.invalid_credentials), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Gestion de l'inscription
        pageInscription.setOnClickListener(v -> {
            Intent intent = new Intent(ConnexionActivity.this, InscriptionActivity.class);
            startActivity(intent);
        });
    }

    private void onSuccessfulLogin(int userId) {
        String token = java.util.UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            // Supprimez les anciens tokens expirés ou liés à cet utilisateur
            db.sessionTokenDao().deleteExpiredTokens(currentTime - TOKEN_VALIDITY_DURATION);
            db.sessionTokenDao().deleteTokenByUserId(userId);

            // Créez un nouveau token
            SessionToken sessionToken = new SessionToken();
            sessionToken.userId = userId;
            sessionToken.token = token;
            sessionToken.timestamp = currentTime;

            db.sessionTokenDao().insert(sessionToken);
        });

        // Sauvegardez l'ID utilisateur dans SharedPreferences
        saveUserId(userId);

        // Redirigez l'utilisateur vers la MainActivity
        Intent intent = new Intent(ConnexionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Executors.newSingleThreadExecutor().execute(() -> {
            int userId = getLastUserId();
            if (userId != -1) {
                SessionToken sessionToken = db.sessionTokenDao().getTokenByUserId(userId);
                if (sessionToken != null && isTokenValid(sessionToken)) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(ConnexionActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }

    private boolean isTokenValid(SessionToken token) {
        long currentTime = System.currentTimeMillis();
        long tokenAge = currentTime - token.timestamp;
        Log.d("TokenDebug", "Token age: " + tokenAge + "ms");
        return tokenAge < TOKEN_VALIDITY_DURATION;
    }

    private int getLastUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    private void saveUserId(int userId) {
        Log.d("ConnexionActivity", "Saving User ID: " + userId);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("USER_ID", userId);
        editor.apply();
    }
}