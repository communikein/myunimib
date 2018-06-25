package it.communikein.myunimib.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.data.model.EnrolledExam;

@Dao
public interface EnrolledExamsDao {

    @Insert
    void add(EnrolledExam... entries);

    @Insert
    void add(ArrayList<EnrolledExam> entries);

    @Query("DELETE FROM enrolled_exams")
    void delete();

    @Delete
    void delete(EnrolledExam... entries);

    @Delete
    void delete(ArrayList<EnrolledExam> entries);

    @Query("DELETE FROM enrolled_exams WHERE adsceId = :adsceId AND appId = :appId " +
            "AND attDidEsaId = :attDidEsaId AND cdsEsaId = :cdsEsaId")
    void delete(int adsceId, int appId, int attDidEsaId, int cdsEsaId);

    @Update
    int update(EnrolledExam... entry);

    @Update
    int update(ArrayList<EnrolledExam> entries);


    @Query("SELECT * FROM enrolled_exams")
    LiveData<List<EnrolledExam>> getObservableEnrolledExams();

    @Query("SELECT * FROM enrolled_exams")
    List<EnrolledExam> getEnrolledExams();

    @Query("SELECT * FROM enrolled_exams WHERE adsceId = :adsceId AND appId = :appId " +
            "AND attDidEsaId = :attDidEsaId AND cdsEsaId = :cdsEsaId")
    LiveData<EnrolledExam> getObservableEnrolledExam(int adsceId, int appId, int attDidEsaId, int cdsEsaId);

    @Query("SELECT * FROM enrolled_exams WHERE adsceId = :adsceId AND appId = :appId " +
            "AND attDidEsaId = :attDidEsaId AND cdsEsaId = :cdsEsaId")
    EnrolledExam getEnrolledExam(int adsceId, int appId, int attDidEsaId, int cdsEsaId);

    @Query("SELECT COUNT(adsceId) FROM enrolled_exams")
    int getEnrolledExamsSize();

}
