package com.communikein.myunimib.sync.enrolledexams;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import com.communikein.myunimib.sync.SyncTask;

import java.util.Date;

/**
 * Created by eliam on 12/15/2017.
 */

public class ExamEnrolledSyncIntentService extends IntentService {

    public ExamEnrolledSyncIntentService() {
        super("ExamEnrolledSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        String now = DateUtils.formatDateTime(context,
                (new Date()).getTime(),
                DateUtils.FORMAT_SHOW_TIME);

        Log.d(SyncUtilsEnrolled.REMINDER_JOB_TAG, now + ": Sync started.");
        SyncTask.syncEnrolledExams(context);
        now = DateUtils.formatDateTime(context,
                (new Date()).getTime(),
                DateUtils.FORMAT_SHOW_TIME);
        Log.d(SyncUtilsEnrolled.REMINDER_JOB_TAG, now + ": Sync ended.");
    }
}
