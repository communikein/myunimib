package it.communikein.myunimib.data.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.Faculty;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.data.model.User;


@Database(entities = {User.class, BookletEntry.class, EnrolledExam.class, AvailableExam.class,
        Building.class, Lesson.class, Faculty.class}, version = 30, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UnimibDatabase extends RoomDatabase {

    public static final String NAME = "S3data";

    public abstract UserDao userDao();
    public abstract BookletDao bookletDao();
    public abstract AvailableExamsDao availableExamsDao();
    public abstract EnrolledExamsDao enrolledExamsDao();
    public abstract LessonsDao lessonsDao();
    public abstract FacultiesDao facultyDao();

    static public final Migration MIGRATION_29_30 = new Migration(29, 30) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // No changes here
        }
    };

}
