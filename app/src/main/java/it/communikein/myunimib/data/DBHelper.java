package it.communikein.myunimib.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import it.communikein.myunimib.data.ExamContract.AvailableExamEntry;
import it.communikein.myunimib.data.ExamContract.EnrolledExamEntry;
import it.communikein.myunimib.data.ExamContract.BookletEntry;


class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "S3data.db";


    DBHelper(final Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
         * This String will contain a simple SQL statement that will create a table that will
         * cache our weather data.
         */
        final String SQL_CREATE_BOOKLET_TABLE = "CREATE TABLE " +
                BookletEntry.TABLE_NAME         + " (" +
                BookletEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BookletEntry.COLUMN_ADSCE_ID    + " INTEGER NOT NULL, " +
                BookletEntry.COLUMN_COURSE_NAME + " TEXT NOT NULL," +
                BookletEntry.COLUMN_CODE        + " TEXT NOT NULL, " +
                BookletEntry.COLUMN_CFU         + " INTEGER, " +
                BookletEntry.COLUMN_DATE        + " INTEGER, " +
                BookletEntry.COLUMN_STATE       + " TEXT, " +
                BookletEntry.COLUMN_MARK        + " TEXT, " +
                "UNIQUE (" + BookletEntry.COLUMN_ADSCE_ID + ") ON CONFLICT FAIL);";

        final String SQL_CREATE_AVAILABLE_EXAMS_TABLE = "CREATE TABLE " +
                AvailableExamEntry.TABLE_NAME               + " (" +
                AvailableExamEntry._ID                      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AvailableExamEntry.COLUMN_COURSE_NAME       + " TEXT NOT NULL," +
                AvailableExamEntry.COLUMN_DESCRIPTION       + " TEXT, " +
                AvailableExamEntry.COLUMN_DATE              + " INTEGER, " +
                AvailableExamEntry.COLUMN_BEGIN_ENROLLMENT  + " INTEGER, " +
                AvailableExamEntry.COLUMN_END_ENROLLMENT    + " INTEGER, " +

                AvailableExamEntry.COLUMN_ADSCE_ID          + " INTEGER, " +
                AvailableExamEntry.COLUMN_CDS_ESA_ID        + " INTEGER, " +
                AvailableExamEntry.COLUMN_ATT_DID_ESA_ID    + " INTEGER, " +
                AvailableExamEntry.COLUMN_APP_ID            + " INTEGER, " +

                "UNIQUE (" + AvailableExamEntry.COLUMN_ADSCE_ID   + ") ON CONFLICT FAIL);";

        final String SQL_CREATE_ENROLLED_EXAMS_TABLE = "CREATE TABLE " +
                EnrolledExamEntry.TABLE_NAME                + " (" +
                EnrolledExamEntry._ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EnrolledExamEntry.COLUMN_COURSE_NAME        + " TEXT NOT NULL," +
                EnrolledExamEntry.COLUMN_DESCRIPTION        + " TEXT, " +
                EnrolledExamEntry.COLUMN_DATE               + " INTEGER, " +
                EnrolledExamEntry.COLUMN_CODE               + " TEXT, " +
                EnrolledExamEntry.COLUMN_BUILDING           + " TEXT, " +
                EnrolledExamEntry.COLUMN_ROOM               + " TEXT, " +
                EnrolledExamEntry.COLUMN_RESERVED           + " TEXT, " +
                EnrolledExamEntry.COLUMN_TEACHERS           + " TEXT, " +

                EnrolledExamEntry.COLUMN_ADSCE_ID           + " INTEGER, " +
                EnrolledExamEntry.COLUMN_CDS_ESA_ID         + " INTEGER, " +
                EnrolledExamEntry.COLUMN_ATT_DID_ESA_ID     + " INTEGER, " +
                EnrolledExamEntry.COLUMN_APP_ID             + " INTEGER, " +

                "UNIQUE (" + EnrolledExamEntry.COLUMN_ADSCE_ID   + ") ON CONFLICT FAIL);";
        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        db.execSQL(SQL_CREATE_BOOKLET_TABLE);
        db.execSQL(SQL_CREATE_AVAILABLE_EXAMS_TABLE);
        db.execSQL(SQL_CREATE_ENROLLED_EXAMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BookletEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AvailableExamEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EnrolledExamEntry.TABLE_NAME);
        onCreate(db);
    }

    static String encodeSQL(String in){
        if (in == null) return "";
        return in.replaceAll("'", "&#39;");
    }

    public static String decodeSQL(String in){
        return in.replaceAll("&#39;", "'");
    }

}
