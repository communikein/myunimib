package com.communikein.myunimib.accountmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by eliam on 12/9/2017.
 */

public class S3AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return (new S3Authenticator(this)).getIBinder();
    }
}
