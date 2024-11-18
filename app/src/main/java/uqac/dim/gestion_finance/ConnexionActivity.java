package uqac.dim.gestion_finance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Utilisateur;

public class ConnexionActivity extends AppCompatActivity {

    private EditText editTextEmailLogin, editTextMotDePasseLogin;
    private Button buttonConnexion;
    private TextView pageInscription;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextMotDePasseLogin = findViewById(R.id.editTextMotDePasseLogin);
        buttonConnexion = findViewById(R.id.buttonConnexion);
        pageInscription = findViewById(R.id.creationC0mpte);

        // Initialisation de la base de données
        db = AppDatabase.getDatabase(getApplicationContext());

        buttonConnexion.setOnClickListener(v -> {
            String loginInput = editTextEmailLogin.getText().toString().trim(); // Peut être email ou nom d'utilisateur
            String motDePasse = editTextMotDePasseLogin.getText().toString().trim();

            // Vérifiez que les champs ne sont pas vides
            if (loginInput.isEmpty() || motDePasse.isEmpty()) {
                Toast.makeText(ConnexionActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifiez si l'utilisateur existe dans la base de données
            Executors.newSingleThreadExecutor().execute(() -> {
                Utilisateur utilisateur = null;

                // Vérifie si loginInput est un email ou un nom d'utilisateur
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(loginInput).matches()) {
                    utilisateur = db.utilisateurDao().getByEmailAndPassword(loginInput, motDePasse); // Recherche par email
                } else {
                    utilisateur = db.utilisateurDao().getByUsernameAndPassword(loginInput, motDePasse); // Recherche par nom d'utilisateur
                }

                final Utilisateur finalUtilisateur = utilisateur;
                runOnUiThread(() -> {
                    if (finalUtilisateur != null) {
                        saveUserId(finalUtilisateur.ID_Utilisateur);
                        Toast.makeText(ConnexionActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                        // Redirection vers MainActivity
                        Intent intent = new Intent(ConnexionActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Ferme l'activité de connexion
                    } else {
                        Toast.makeText(ConnexionActivity.this, "Email/Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        pageInscription.setOnClickListener(v -> {
            Intent intent = new Intent(ConnexionActivity.this, InscriptionActivity.class);
            startActivity(intent);
        });
    }

    private void saveUserId(int userId) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("USER_ID", userId);
        editor.apply();
    }
}