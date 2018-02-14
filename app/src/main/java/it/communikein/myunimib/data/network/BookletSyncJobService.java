package it.communikein.myunimib.data.network;

import android.app.Service;
import android.util.Log;

import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasServiceInjector;
import it.communikein.myunimib.data.UnimibRepository;

@SuppressWarnings({"WeakerAccess", "unused"})
public class BookletSyncJobService extends JobService implements HasServiceInjector {

    private static final String LOG_TAG = BookletSyncJobService.class.getSimpleName();

    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidInjector;

    @Inject
    UnimibRepository repository;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

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
        repository.fetchBooklet();

        jobFinished(jobParameters, false);

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


    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidInjector;
    }
}
