package it.communikein.myunimib.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface UnimibDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void bulkInsertBooklet(List<BookletEntry> entry);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void bulkInsertAvailableExams(List<AvailableExam> entry);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void bulkInsertEnrolledExams(List<EnrolledExam> entry);


    @Query("DELETE FROM booklet")
    void deleteBooklet();

    @Query("DELETE FROM available_exams")
    void deleteAvailableExams();

    @Query("DELETE FROM enrolled_exams")
    void deleteEnrolledExams();


    @Query("SELECT adsceId, name, score, state FROM booklet")
    DataSource.Factory<Integer, ListBookletEntry> getBooklet();

    @Query("SELECT adsceId, name, date, description FROM available_exams")
    DataSource.Factory<Integer, ListAvailableExam> getAvailableExams();

    @Query("SELECT adsceId, name, date, description FROM enrolled_exams")
    DataSource.Factory<Integer, ListEnrolledExam> getEnrolledExams();


    @Query("SELECT * FROM booklet WHERE adsceId = :adsceId")
    BookletEntry getBookletEntry(int adsceId);

    @Query("SELECT * FROM available_exams WHERE adsceId = :adsceId")
    AvailableExam getAvailableExam(int adsceId);

    @Query("SELECT * FROM enrolled_exams WHERE adsceId = :adsceId")
    LiveData<EnrolledExam> getEnrolledExam(int adsceId);


    @Query("SELECT UPPER(name) FROM booklet WHERE UPPER(name) LIKE UPPER(:name)")
    List<String> getCoursesNames(String name);


    @Update
    void updateBookletEntry(BookletEntry entry);

    @Update
    void updateAvailableExam(AvailableExam entry);

    @Update
    void updateEnrolledExam(EnrolledExam entry);
}
