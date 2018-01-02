package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;


@Database(entities = {BookletEntry.class, EnrolledExam.class, AvailableExam.class},
        version = 22, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UnimibDatabase extends RoomDatabase {

    public static final String NAME = "S3data";

    public abstract UnimibDao unimibDao();
}
