package uqac.dim.gestion_finance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Utilisateur;

public class InscriptionActivity extends AppCompatActivity {

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
                Toast.makeText(InscriptionActivity.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            // Insérez l'utilisateur dans la base de données
            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, getString(R.string.database_name)).build();

            new Thread(() -> {
                // Vérifiez si un utilisateur avec cet email existe déjà
                Utilisateur utilisateurExistant = db.utilisateurDao().getByEmail(email);

                if (utilisateurExistant != null) {
                    // L'email est déjà utilisé
                    runOnUiThread(() -> Toast.makeText(InscriptionActivity.this, getString(R.string.email_already_used), Toast.LENGTH_SHORT).show());
                } else {
                    // Insérez l'utilisateur
                    Utilisateur nouvelUtilisateur = new Utilisateur();
                    nouvelUtilisateur.Nom = nom;
                    nouvelUtilisateur.Email = email;
                    nouvelUtilisateur.Mot_de_passe = motDePasse;

                    db.utilisateurDao().insert(nouvelUtilisateur);
                    runOnUiThread(() -> Toast.makeText(InscriptionActivity.this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        pageConnexion.setOnClickListener(v -> {
            Intent intent = new Intent(InscriptionActivity.this, ConnexionActivity.class);
            startActivity(intent);
        });
    }
}