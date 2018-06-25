package it.communikein.myunimib.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.data.model.AvailableExam;

@Dao
public interface AvailableExamsDao {

    @Insert
    void add(AvailableExam... entries);

    @Insert
    void add(ArrayList<AvailableExam> entries);

    @Query("DELETE FROM available_exams")
    void delete();

    @Delete
    void delete(AvailableExam... entries);

    @Delete
    void delete(ArrayList<AvailableExam> entries);

    @Query("DELETE FROM available_exams WHERE adsceId = :adsceId AND appId = :appId " +
            "AND attDidEsaId = :attDidEsaId AND cdsEsaId = :cdsEsaId")
    void delete(int adsceId, int appId, int attDidEsaId, int cdsEsaId);

    @Update
    int update(AvailableExam... entry);

    @Update
    int update(ArrayList<AvailableExam> entries);


    @Query("SELECT * FROM available_exams")
    LiveData<List<AvailableExam>> getObservableAvailableExams();

    @Query("SELECT * FROM available_exams")
    List<AvailableExam> getAvailableExams();

    @Query("SELECT * FROM available_exams WHERE adsceId = :adsceId AND appId = :appId " +
            "AND attDidEsaId = :attDidEsaId AND cdsEsaId = :cdsEsaId")
    LiveData<AvailableExam> getObservableAvailableExam(int adsceId, int appId, int attDidEsaId, int cdsEsaId);

    @Query("SELECT * FROM available_exams WHERE adsceId = :adsceId AND appId = :appId " +
            "AND attDidEsaId = :attDidEsaId AND cdsEsaId = :cdsEsaId")
    AvailableExam getAvailableExam(int adsceId, int appId, int attDidEsaId, int cdsEsaId);

    @Query("SELECT COUNT(adsceId) FROM available_exams")
    int getAvailableExamsSize();

}
