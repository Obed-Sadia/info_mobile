package uqac.dim.gestion_finance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.SessionToken;
import uqac.dim.gestion_finance.entities.Utilisateur;

public class ConnexionActivity extends AppCompatActivity {

    private static final long TOKEN_VALIDITY_DURATION = 24 * 60 * 60 * 1000; // 24h
    private static final String TAG = "ConnexionActivity";

    private EditText editTextEmailLogin, editTextMotDePasseLogin;
    private Button buttonConnexion;
    private TextView pageInscription;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connexion);

        // Initialisation des vues
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextMotDePasseLogin = findViewById(R.id.editTextMotDePasseLogin);
        buttonConnexion = findViewById(R.id.buttonConnexion);
        pageInscription = findViewById(R.id.creationC0mpte);

        db = AppDatabase.getDatabase(getApplicationContext());

        // Gestion de la connexion
        buttonConnexion.setOnClickListener(v -> {
            String loginInput = editTextEmailLogin.getText().toString().trim();
            String motDePasse = editTextMotDePasseLogin.getText().toString().trim();

            // Vérifiez que les champs ne sont pas vides
            if (loginInput.isEmpty() || motDePasse.isEmpty()) {
                showErrorDialog(getString(R.string.error_title), getString(R.string.fill_all_fields));
                return;
            }

            // Vérifiez si l'utilisateur existe dans la base de données
            Executors.newSingleThreadExecutor().execute(() -> {
                Utilisateur utilisateur = null;

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
                        showErrorDialog(getString(R.string.error_title), getString(R.string.invalid_credentials));
                    }
                });
            });
        });

        pageInscription.setOnClickListener(v -> {
            Intent intent = new Intent(ConnexionActivity.this, InscriptionActivity.class);
            startActivity(intent);
        });
    }

    private void onSuccessfulLogin(int userId) {
        String token = java.util.UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            db.sessionTokenDao().deleteExpiredTokens(currentTime - TOKEN_VALIDITY_DURATION);
            db.sessionTokenDao().deleteTokenByUserId(userId);

            SessionToken sessionToken = new SessionToken();
            sessionToken.userId = userId;
            sessionToken.token = token;
            sessionToken.timestamp = currentTime;

            db.sessionTokenDao().insert(sessionToken);
        });

        saveUserId(userId);

        // Rediriger vers la MainActivity
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_success), Snackbar.LENGTH_SHORT).show();
        Intent intent = new Intent(ConnexionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private int getLastUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    private void saveUserId(int userId) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("USER_ID", userId);
        editor.apply();
    }
}