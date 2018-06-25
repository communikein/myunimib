package it.communikein.myunimib.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.data.model.Faculty;

@Dao
public interface FacultiesDao {

    @Insert
    void add(Faculty... faculty);

    @Insert
    void add(ArrayList<Faculty> faculties);

    @Query("DELETE FROM faculties")
    void delete();

    @Delete
    void delete(Faculty... faculties);

    @Delete
    void delete(ArrayList<Faculty> faculties);

    @Query("DELETE FROM faculties WHERE code = :code")
    void delete(int code);

    @Update
    int update(Faculty... faculties);

    @Update
    int update(ArrayList<Faculty> faculties);


    @Query("SELECT * FROM faculties")
    LiveData<List<Faculty>> getObservableAllFaculties();

    @Query("SELECT * FROM faculties WHERE userId = :userId")
    LiveData<List<Faculty>> getObservableFacultiesForUser(String userId);

    @Query("SELECT * FROM faculties")
    List<Faculty> getAllFaculties();

    @Query("SELECT * FROM faculties WHERE userId = :userId")
    List<Faculty> getFacultiesForUser(String userId);

    @Query("SELECT * FROM faculties WHERE code = :code")
    Faculty getFaculty(int code);

    @Query("SELECT * FROM faculties WHERE code = :code")
    LiveData<Faculty> getObservableFaculty(int code);

    @Query("SELECT COUNT(code) FROM faculties WHERE userId = :userId")
    int getNumberOfFacultiesForUser(String userId);

}
