package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.User;


@Database(entities = {User.class, BookletEntry.class, EnrolledExam.class, AvailableExam.class,
        Building.class}, version = 25, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UnimibDatabase extends RoomDatabase {

    public static final String NAME = "S3data";

    public abstract UnimibDao unimibDao();
}
