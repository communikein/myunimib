package com.communikein.myunimib.sync.enrolledexams;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.communikein.myunimib.data.ExamContract;
import com.communikein.myunimib.utilities.PreferenceUtils;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;


public class SyncUtilsEnrolled {
    private static final int SYNC_INTERVAL_MAX_MINUTES = 10;
    private static int SYNC_INTERVAL_MIN_SECONDS;
    private static final int SYNC_INTERVAL_MAX_SECONDS =
            (int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MAX_MINUTES);
    private static int SYNC_WINDOW_SECONDS;

    public static final String REMINDER_JOB_TAG = "sync-enrolled-exams";

    private static boolean sInitialized;

    synchronized private static void scheduleEnrolledExamsSync(Context context) {
        if (sInitialized) return;

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(ExamEnrolledSyncJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_MIN_SECONDS, SYNC_WINDOW_SECONDS))
                .setRecurring(true)
                .build();

        dispatcher.schedule(constraintReminderJob);

        sInitialized = true;
    }


    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver
     */
    synchronized public static void initialize(@NonNull final Context context) {

        /*
         * Only perform initialization once per app lifetime. If initialization has already been
         * performed, we have nothing to do in this method.
         */
        if (sInitialized) return;

        sInitialized = true;

        /*
         * Get the preferred sync frequency from the user preferences
         */
        int SYNC_INTERVAL_MIN_MINUTES = PreferenceUtils.getPreferredSyncFrequency(context);
        SYNC_INTERVAL_MIN_SECONDS = (int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MIN_MINUTES);
        SYNC_WINDOW_SECONDS = SYNC_INTERVAL_MIN_SECONDS + SYNC_INTERVAL_MAX_SECONDS;

        /*
         * This method call triggers the app to create its task to synchronize the booklet data
         * periodically.
         */
        scheduleEnrolledExamsSync(context);

        /*
         * We need to check to see if our ContentProvider has data to display in our list.
         * However, performing a query on the main thread is a bad idea as this may cause
         * our UI to lag. Therefore, we create a thread in which we will run the query
         * to check the contents of our ContentProvider.
         */
        Thread checkForEmpty = new Thread(() -> {

            /* URI for every row of the booklet in the table*/
            Uri examsQueryUri = ExamContract.EnrolledExamEntry.CONTENT_URI;

            /*
             * Since this query is going to be used only as a check to see if we have any
             * data (rather than to display data), we just need to PROJECT the ID of each
             * row. In our queries where we display data, we need to PROJECT more columns
             * to determine what weather details need to be displayed.
             */
            String[] projectionColumns = {ExamContract.ExamEntry._ID};

            /* Here, we perform the query to check to see if we have any weather data */
            Cursor cursor = context.getContentResolver().query(
                    examsQueryUri,
                    projectionColumns,
                    null,
                    null,
                    null);
            /*
             * If the Cursor was null OR if it was empty, we need to sync immediately to
             * be able to display data to the user.
             */
            if (null == cursor || cursor.getCount() == 0) {
                startImmediateSync(context);
            }

            /* Make sure to close the Cursor to avoid memory leaks! */
            if (cursor != null) cursor.close();
        });

        /* Finally, once the thread is prepared, fire it off to perform our checks. */
        checkForEmpty.start();
    }

    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */
    private static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, ExamEnrolledSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
