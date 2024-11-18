package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import uqac.dim.gestion_finance.entities.Utilisateur;

@Dao
public interface UtilisateurDao {
    @Insert
    void insert(Utilisateur utilisateur);

    @Query("SELECT * FROM Utilisateur")
    List<Utilisateur> getAll();

    @Query("SELECT * FROM Utilisateur WHERE ID_Utilisateur = :id")
    Utilisateur getById(int id);

    @Query("DELETE FROM Utilisateur")
    void deleteAll();

    @Query("SELECT * FROM Utilisateur WHERE Email = :email")
    Utilisateur getByEmail(String email);

    @Query("SELECT * FROM Utilisateur WHERE Email = :email AND Mot_de_passe = :password")
    Utilisateur getByEmailAndPassword(String email, String password);

    @Query("SELECT * FROM Utilisateur WHERE Nom = :username AND Mot_de_passe = :password")
    Utilisateur getByUsernameAndPassword(String username, String password);
}