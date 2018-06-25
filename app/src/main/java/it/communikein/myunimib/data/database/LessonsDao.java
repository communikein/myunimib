package it.communikein.myunimib.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.data.model.Lesson;

@Dao
public interface LessonsDao {

    @Insert
    void add(Lesson... lesson);

    @Insert
    void add(ArrayList<Lesson> lesson);

    @Query("DELETE FROM lessons")
    void delete();

    @Delete
    void delete(Lesson... lessons);

    @Delete
    void delete(ArrayList<Lesson> lessons);

    @Query("DELETE FROM lessons WHERE id = :id")
    void delete(int id);

    @Update
    int update(Lesson... lessons);

    @Update
    int update(ArrayList<Lesson> lesson);


    @Query("SELECT name FROM booklet WHERE name LIKE :name")
    LiveData<List<String>> getCoursesNames(String name);

    @Query("SELECT * FROM lessons")
    LiveData<List<Lesson>> getObservableTimetable();

    @Query("SELECT * FROM lessons WHERE dayOfWeek= :day_of_week ORDER BY timeStart ASC")
    LiveData<List<Lesson>> getObservableTimetableOfDay(String day_of_week);

    @Query("SELECT * FROM lessons")
    List<Lesson> getTimetable();

    @Query("SELECT * FROM lessons WHERE dayOfWeek= :day_of_week ORDER BY timeStart ASC")
    List<Lesson> getTimetableOfDay(String day_of_week);

    @Query("SELECT * FROM lessons WHERE id = :id")
    Lesson getLesson(int id);

    @Query("SELECT * FROM lessons WHERE id = :id")
    LiveData<Lesson> getObservableLesson(int id);

    @Query("SELECT COUNT(courseName) FROM lessons")
    int getTimetableSize();

}
