package it.communikein.myunimib.utilities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.test.suitebuilder.annotation.Suppress;

import it.communikein.myunimib.R;

/**
 * Created by eliam on 12/22/2017.
 */

public class NotificationHelper extends ContextWrapper {

    private NotificationManager notificationManager;

    public final String CHANNEL_BOOKLET_ID;
    public final String CHANNEL_BOOKLET_NAME;
    public final String CHANNEL_AVAILABLE_EXAMS_ID;
    public final String CHANNEL_AVAILABLE_EXAMS_NAME;
    public final String CHANNEL_ENROLLED_EXAMS_ID;
    public final String CHANNEL_ENROLLED_EXAMS_NAME;


    public NotificationHelper(Context base) {
        super(base);

        CHANNEL_BOOKLET_ID = getString(R.string.channel_booklet_id);
        CHANNEL_BOOKLET_NAME = base.getString(R.string.channel_booklet_name);

        CHANNEL_AVAILABLE_EXAMS_ID = getString(R.string.channel_available_exams_id);
        CHANNEL_AVAILABLE_EXAMS_NAME = base.getString(R.string.channel_available_exams_name);

        CHANNEL_ENROLLED_EXAMS_ID = getString(R.string.channel_enrolled_exams_id);
        CHANNEL_ENROLLED_EXAMS_NAME = base.getString(R.string.channel_enrolled_exams_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
    }

    @TargetApi(26)
    public void createChannels() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_BOOKLET_ID,
                CHANNEL_BOOKLET_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(channel);

        channel = new NotificationChannel(CHANNEL_AVAILABLE_EXAMS_ID,
                CHANNEL_AVAILABLE_EXAMS_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setShowBadge(true);
        getManager().createNotificationChannel(channel);

        channel = new NotificationChannel(CHANNEL_ENROLLED_EXAMS_ID,
                CHANNEL_ENROLLED_EXAMS_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setShowBadge(true);
        getManager().createNotificationChannel(channel);
    }


    public Notification.Builder getNotificationBooklet(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), CHANNEL_BOOKLET_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true);
        }
        else {
            return new Notification.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true);
        }
    }

    public Notification.Builder getNotificationAvailable(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), CHANNEL_AVAILABLE_EXAMS_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true);
        }
        else {
            return new Notification.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true);
        }
    }

    public Notification.Builder getNotificationEnrolled(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), CHANNEL_ENROLLED_EXAMS_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true);
        }
        else {
            return new Notification.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true);
        }
    }


    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }


    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }
}
