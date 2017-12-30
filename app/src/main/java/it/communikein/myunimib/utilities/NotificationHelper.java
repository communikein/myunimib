package it.communikein.myunimib.utilities;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import it.communikein.myunimib.R;

/**
 * Created by eliam on 12/22/2017.
 */

public class NotificationHelper extends ContextWrapper {

    private static final String LOG_TAG = NotificationHelper.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static NotificationHelper sInstance;

    private Context mContext;
    private static NotificationManager notificationManager;

    private final String CHANNEL_BOOKLET_ID;
    private final String CHANNEL_BOOKLET_NAME;
    private final String CHANNEL_AVAILABLE_EXAMS_ID;
    private final String CHANNEL_AVAILABLE_EXAMS_NAME;
    private final String CHANNEL_ENROLLED_EXAMS_ID;
    private final String CHANNEL_ENROLLED_EXAMS_NAME;


    private NotificationHelper(Context base) {
        super(base);

        mContext = base;

        CHANNEL_BOOKLET_ID = getString(R.string.channel_booklet_id);
        CHANNEL_BOOKLET_NAME = base.getString(R.string.channel_booklet_name);

        CHANNEL_AVAILABLE_EXAMS_ID = getString(R.string.channel_available_exams_id);
        CHANNEL_AVAILABLE_EXAMS_NAME = base.getString(R.string.channel_available_exams_name);

        CHANNEL_ENROLLED_EXAMS_ID = getString(R.string.channel_enrolled_exams_id);
        CHANNEL_ENROLLED_EXAMS_NAME = base.getString(R.string.channel_enrolled_exams_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
    }

    public synchronized static NotificationHelper getInstance(Context context) {
        Log.d(LOG_TAG, "Getting the notification helper");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new NotificationHelper(context);
                Log.d(LOG_TAG, "Made new notification helper");
            }
        }
        return sInstance;
    }

    @TargetApi(26)
    private void createChannels() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_BOOKLET_ID,
                CHANNEL_BOOKLET_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
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


    public NotificationCompat.Builder getNotificationBookletChanges(PendingIntent pendingIntent) {
        String title = mContext.getString(R.string.channel_booklet_name);
        String content = mContext.getString(R.string.channel_booklet_content_changes);

        return getNotification(CHANNEL_ENROLLED_EXAMS_ID, title, content, pendingIntent);
    }

    public NotificationCompat.Builder getNotificationAvailableChanges(PendingIntent pendingIntent) {
        String title = mContext.getString(R.string.channel_available_exams_name);
        String content = mContext.getString(R.string.channel_available_exams_content_changes);

        return getNotification(CHANNEL_ENROLLED_EXAMS_ID, title, content, pendingIntent);
    }

    public NotificationCompat.Builder getNotificationEnrolledChanges(PendingIntent pendingIntent) {
        String title = mContext.getString(R.string.channel_enrolled_exams_name);
        String content = mContext.getString(R.string.channel_enrolled_exams_content_changes);

        return getNotification(CHANNEL_ENROLLED_EXAMS_ID, title, content, pendingIntent);
    }

    private NotificationCompat.Builder getNotification(String channelId, String title, String content,
                                                              PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }
        else {
            return new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }
    }


    public void notify(int id, NotificationCompat.Builder notification) {
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
