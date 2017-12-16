package com.communikein.myunimib.sync.availableexams;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import com.communikein.myunimib.sync.SyncTask;

import java.util.Date;


public class ExamAvailableSyncIntentService extends IntentService {

    public ExamAvailableSyncIntentService() {
        super("ExamAvailableSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
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
    }
}