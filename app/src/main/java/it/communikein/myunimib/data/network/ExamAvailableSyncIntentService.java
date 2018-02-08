package it.communikein.myunimib.data.network;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasServiceInjector;
import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.User;


public class ExamAvailableSyncIntentService extends IntentService implements HasServiceInjector {

    private static final String LOG_TAG = ExamAvailableSyncIntentService.class.getSimpleName();

    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidInjector;

    @Inject
    UnimibRepository repository;


    public ExamAvailableSyncIntentService() {
        super("ExamAvailableSyncIntentService");
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOG_TAG, "Intent service started");

        repository.fetchAvailableExams();
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidInjector;
    }
}