package it.communikein.myunimib.data.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import it.communikein.myunimib.utilities.InjectorUtils;


public class ExamEnrolledSyncIntentService extends IntentService {

    private static final String LOG_TAG = ExamEnrolledSyncIntentService.class.getSimpleName();

    public ExamEnrolledSyncIntentService() {
        super("ExamEnrolledSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOG_TAG, "Intent service started");

        UnimibNetworkDataSource networkDataSource = InjectorUtils
                .provideNetworkDataSource(this.getApplicationContext());
        networkDataSource.fetchEnrolledExams();
    }
}
