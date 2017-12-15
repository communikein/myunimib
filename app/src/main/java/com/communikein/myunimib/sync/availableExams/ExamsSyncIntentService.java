package com.communikein.myunimib.sync.availableExams;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.communikein.myunimib.sync.SyncTask;

/**
 * Created by eliam on 12/7/2017.
 */

public class ExamsSyncIntentService extends IntentService {

    public ExamsSyncIntentService() {
        super("ExamsSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SyncTask.syncExams(this);
    }
}