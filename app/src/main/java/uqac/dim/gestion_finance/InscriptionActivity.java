package uqac.dim.gestion_finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import androidx.room.Room;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Utilisateur;

public class InscriptionActivity extends AppCompatActivity {

    private static final String TAG = "InscriptionActivity";

    private EditText editTextNom, editTextEmail, editTextMotDePasse;
    private Button buttonInscription;
    private TextView pageConnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inscription);

        editTextNom = findViewById(R.id.editTextNom);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextMotDePasse = findViewById(R.id.editTextMotDePasse);
        buttonInscription = findViewById(R.id.buttonInscription);
        pageConnexion = findViewById(R.id.connexionCompte);

        buttonInscription.setOnClickListener(v -> {
            String nom = editTextNom.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String motDePasse = editTextMotDePasse.getText().toString().trim();

            // Vérifiez que les champs ne sont pas vides
            if (nom.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
                showErrorDialog(getString(R.string.error_title), getString(R.string.fill_all_fields));
                return;
            }

            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, getString(R.string.database_name)).build();

            new Thread(() -> {
                Utilisateur utilisateurExistant = db.utilisateurDao().getByEmail(email);

                if (utilisateurExistant != null) {
                    runOnUiThread(() -> showErrorDialog(getString(R.string.error_title), getString(R.string.email_already_used)));
                } else {
                    Utilisateur nouvelUtilisateur = new Utilisateur();
                    nouvelUtilisateur.Nom = nom;
                    nouvelUtilisateur.Email = email;
                    nouvelUtilisateur.Mot_de_passe = motDePasse;

                    db.utilisateurDao().insert(nouvelUtilisateur);

                    runOnUiThread(() -> {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.registration_success), Snackbar.LENGTH_SHORT).show();
                        Intent intent = new Intent(InscriptionActivity.this, ConnexionActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }).start();
        });

        pageConnexion.setOnClickListener(v -> {
            Intent intent = new Intent(InscriptionActivity.this, ConnexionActivity.class);
            startActivity(intent);
        });
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }
}