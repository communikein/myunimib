package it.communikein.myunimib.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import it.communikein.myunimib.data.model.User;

@Dao
public interface UserDao {

    @Insert
    void add(User... entries);

    @Update
    int update(User... entries);

    @Query("DELETE FROM user")
    void delete();

    @Delete
    void delete(User... entries);

    @Query("SELECT * FROM user WHERE username = :username")
    LiveData<User> getObservableUser(String username);

    @Query("SELECT * FROM user WHERE username = :username")
    User getUser(String username);

    @Query("SELECT COUNT(username) FROM user")
    int getUsersCount();

}
