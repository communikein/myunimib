package it.communikein.myunimib.data.network;

import android.util.Log;

import com.crashlytics.android.answers.FirebaseAnalyticsEvent;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

import it.communikein.myunimib.R;
import it.communikein.myunimib.utilities.InjectorUtils;
import it.communikein.myunimib.utilities.NotificationHelper;


public class BookletSyncJobService extends JobService {

    private static final String LOG_TAG = BookletSyncJobService.class.getSimpleName();

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
        Log.d(LOG_TAG, "Scheduled booklet job started.");

        UnimibNetworkDataSource unimibNetworkDataSource = InjectorUtils
                .provideNetworkDataSource(this.getApplicationContext());
        unimibNetworkDataSource.fetchBooklet();

        jobFinished(jobParameters, false);

        Log.d(LOG_TAG, "Scheduled booklet job finished.");

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
        Log.d(LOG_TAG, "Scheduled booklet job stopped.");

        return true;
    }

}
