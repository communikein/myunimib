package com.communikein.myunimib.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.communikein.myunimib.utilities.MyunimibDateUtils;

/**
 * Created by eliam on 12/6/2017.
 */

public class MyUnimibProvider extends ContentProvider {

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int CODE_BOOKLET_ALL = 100;
    public static final int CODE_BOOKLET_WITH_ID = 101;
    public static final int CODE_COURSES_NAMES = 102;

    public static final int CODE_AVAILABLE_EXAMS_ALL = 200;
    public static final int CODE_AVAILABLE_EXAMS_WITH_ID = 201;

    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of WeatherProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DBHelper mOpenHelper;

    /**
     * Creates the UriMatcher that will match each URI to the CODE_WEATHER and
     * CODE_WEATHER_WITH_DATE constants defined above.
     * <p>
     * It's possible you might be thinking, "Why create a UriMatcher when you can use regular
     * expressions instead? After all, we really just need to match some patterns, and we can
     * use regular expressions to do that right?" Because you're not crazy, that's why.
     * <p>
     * UriMatcher does all the hard work for you. You just have to tell it which code to match
     * with which URI, and it does the rest automagically. Remember, the best programmers try
     * to never reinvent the wheel. If there is a solution for a problem that exists and has
     * been tested and proven, you should almost always use it unless there is a compelling
     * reason not to.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_WEATHER and CODE_WEATHER_WITH_DATE
     */
    public static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ExamContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ExamContract.PATH_BOOKLET, CODE_BOOKLET_ALL);
        matcher.addURI(authority, ExamContract.PATH_BOOKLET + "/#", CODE_BOOKLET_WITH_ID);
        matcher.addURI(authority, ExamContract.PATH_BOOKLET_COURSES, CODE_COURSES_NAMES);

        matcher.addURI(authority,
                ExamContract.PATH_EXAMS_AVAILABLE, CODE_AVAILABLE_EXAMS_ALL);
        matcher.addURI(authority,
                ExamContract.PATH_EXAMS_AVAILABLE + "/#", CODE_AVAILABLE_EXAMS_WITH_ID);

        return matcher;
    }

    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     *
     * Nontrivial initialization (such as opening, upgrading, and scanning
     * databases) should be deferred until the content provider is used (via {@link #query},
     * {@link #bulkInsert(Uri, ContentValues[])}, etc).
     *
     * Deferred initialization keeps application startup fast, avoids unnecessary work if the
     * provider turns out not to be needed, and stops database errors (such as a full disk) from
     * halting application launch.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        /*
         * As noted in the comment above, onCreate is run on the main thread, so performing any
         * lengthy operations will cause lag in your app. Since WeatherDbHelper's constructor is
         * very lightweight, we are safe to perform that initialization here.
         */
        mOpenHelper = new DBHelper(getContext());
        return true;
    }



    /**
     * Handles requests to insert a set of new rows. .
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     *
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String TABLE_NAME;
        switch (sUriMatcher.match(uri)) {
            case CODE_BOOKLET_ALL:
                TABLE_NAME = ExamContract.BookletEntry.TABLE_NAME;
                break;

            case CODE_AVAILABLE_EXAMS_ALL:
                TABLE_NAME = ExamContract.AvailableExamEntry.TABLE_NAME;
                break;

            default:
                return super.bulkInsert(uri, values);
        }

        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(TABLE_NAME, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (rowsInserted > 0) {
            Context context = getContext();
            if (context != null)
                context.getContentResolver().notifyChange(uri, null);
        }

        return rowsInserted;
    }

    /**
     * Handles query requests from clients..
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     *                      included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the
     *                      selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_BOOKLET_WITH_ID: {
                String id = uri.getLastPathSegment();

                String[] selectionArguments = new String[]{id};

                cursor = mOpenHelper.getReadableDatabase().query(
                        ExamContract.BookletEntry.TABLE_NAME,
                        projection,
                        ExamContract.BookletEntry.COLUMN_ADSCE_ID+ " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_BOOKLET_ALL: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ExamContract.BookletEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_COURSES_NAMES: {
                String description = uri.getLastPathSegment();

                projection = new String[]{ExamContract.BookletEntry.COLUMN_COURSE_NAME};
                selectionArgs = new String[]{DBHelper.encodeSQL(description)};

                selection = "UPPER(" + ExamContract.BookletEntry.COLUMN_COURSE_NAME + ") " +
                        "LIKE UPPER('%?%')";

                cursor = mOpenHelper.getReadableDatabase().query(
                        ExamContract.BookletEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_AVAILABLE_EXAMS_WITH_ID: {
                String id = uri.getLastPathSegment();

                String[] selectionArguments = new String[]{id};

                cursor = mOpenHelper.getReadableDatabase().query(
                        ExamContract.AvailableExamEntry.TABLE_NAME,
                        projection,
                        ExamContract.AvailableExamEntry.COLUMN_ADSCE_ID+ " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_AVAILABLE_EXAMS_ALL: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ExamContract.AvailableExamEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        String TABLE_NAME;
        switch (sUriMatcher.match(uri)) {
            case CODE_BOOKLET_ALL:
                TABLE_NAME = ExamContract.BookletEntry.TABLE_NAME;
                break;

            case CODE_AVAILABLE_EXAMS_ALL:
                TABLE_NAME = ExamContract.AvailableExamEntry.TABLE_NAME;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                TABLE_NAME,
                selection,
                selectionArgs);

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in MyUNIMIB.");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String TABLE_NAME;
        switch (sUriMatcher.match(uri)) {
            case CODE_BOOKLET_ALL:
                TABLE_NAME = ExamContract.BookletEntry.TABLE_NAME;
                break;

            case CODE_AVAILABLE_EXAMS_ALL:
                TABLE_NAME = ExamContract.AvailableExamEntry.TABLE_NAME;
                break;

            default:
                return null;
        }

        db.beginTransaction();
        int rowsInserted = 0;
        try {
            long _id = db.insert(TABLE_NAME, null, values);
            if (_id != -1) {
                rowsInserted++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        if (rowsInserted > 0)
            return uri;
        else
            return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("We are not implementing update in MyUNIMIB");
    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}
