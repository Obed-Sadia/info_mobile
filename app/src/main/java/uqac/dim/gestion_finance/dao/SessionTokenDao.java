package uqac.dim.gestion_finance.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import uqac.dim.gestion_finance.entities.SessionToken;

@Dao
public interface SessionTokenDao {

    @Insert
    void insert(SessionToken sessionToken);

    @Update
    void update(SessionToken sessionToken);

    @Query("SELECT * FROM SessionToken WHERE userId = :userId LIMIT 1")
    SessionToken getTokenByUserId(int userId);

    @Query("DELETE FROM SessionToken")
    void clearAllTokens();

    @Query("DELETE FROM SessionToken WHERE timestamp < :expiryTime")
    void deleteExpiredTokens(long expiryTime);

    @Query("DELETE FROM SessionToken WHERE userId = :userId")
    void deleteTokenByUserId(int userId);
}
