package it.communikein.myunimib.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.data.model.BookletEntry;

@Dao
public interface BookletDao {

    @Insert
    void add(BookletEntry... entries);

    @Insert
    void add(ArrayList<BookletEntry> entries);

    @Update
    int update(BookletEntry... entries);

    @Update
    int update(ArrayList<BookletEntry> entries);

    @Query("DELETE FROM booklet")
    void delete();

    @Delete
    void delete(BookletEntry... entries);

    @Delete
    void delete(ArrayList<BookletEntry> entries);

    @Query("DELETE FROM booklet WHERE id = :id")
    void delete(int id);

    @Query("SELECT * FROM booklet WHERE fake = 0")
    LiveData<List<BookletEntry>> getObservableBooklet();

    @Query("SELECT * FROM booklet WHERE fake = 1")
    LiveData<List<BookletEntry>> getObservableFakeBooklet();

    @Query("SELECT * FROM booklet WHERE fake = 0")
    List<BookletEntry> getRealBooklet();

    @Query("SELECT * FROM booklet WHERE fake = 1")
    List<BookletEntry> getFakeBooklet();

    @Query("SELECT * FROM booklet WHERE adsceId = :adsceId")
    BookletEntry getBookletEntry(int adsceId);

    @Query("SELECT COUNT(adsceId) FROM booklet")
    int getBookletSize();

}
