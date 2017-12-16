package com.communikein.myunimib.sync.availableexams;

import android.content.Context;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;

import com.communikein.myunimib.sync.SyncTask;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

import java.util.Date;


public class ExamAvailableSyncJobService extends JobService {

    private AsyncTask<Void, Void, Void> mFetchDataTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        mFetchDataTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                String now = DateUtils.formatDateTime(context,
                        (new Date()).getTime(),
                        DateUtils.FORMAT_SHOW_TIME);

                Log.d(SyncUtilsAvailable.REMINDER_JOB_TAG, now + ": Sync started.");
                SyncTask.syncAvailableExams(context);
                now = DateUtils.formatDateTime(context,
                        (new Date()).getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                Log.d(SyncUtilsAvailable.REMINDER_JOB_TAG, now + ": Sync ended.");

                jobFinished(jobParameters, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        mFetchDataTask.execute();
        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mFetchDataTask != null) {
            mFetchDataTask.cancel(true);
        }
        return true;
    }

}
