package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;


@Database(entities = {BookletEntry.class, EnrolledExam.class, AvailableExam.class},
        version = 21, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UnimibDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "S3data";

    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static volatile UnimibDatabase sInstance;

    public static UnimibDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            UnimibDatabase.class, UnimibDatabase.DATABASE_NAME).build();
                }
            }
        }

        return sInstance;
    }

    public abstract UnimibDao unimibDao();

}
