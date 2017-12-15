package com.communikein.myunimib.sync.booklet;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.communikein.myunimib.sync.SyncTask;

/**
 * Created by eliam on 12/6/2017.
 */

public class BookletSyncIntentService extends IntentService {

    public BookletSyncIntentService() {
        super("BookletSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SyncTask.syncBooklet(this);
    }
}
