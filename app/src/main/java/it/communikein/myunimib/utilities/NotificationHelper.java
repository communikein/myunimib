package it.communikein.myunimib.utilities;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import javax.inject.Inject;
import javax.inject.Singleton;

import it.communikein.myunimib.R;
import it.communikein.myunimib.ui.MainActivity;

/**
 * Created by eliam on 12/22/2017.
 */

@Singleton
public class NotificationHelper extends ContextWrapper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    private static NotificationManager notificationManager;

    private final String CHANNEL_BOOKLET_ID;
    private final String CHANNEL_BOOKLET_NAME;
    private final String CHANNEL_AVAILABLE_EXAMS_ID;
    private final String CHANNEL_AVAILABLE_EXAMS_NAME;
    private final String CHANNEL_ENROLLED_EXAMS_ID;
    private final String CHANNEL_ENROLLED_EXAMS_NAME;

    private static final int BOOKLET_CHANGES_NOTIFICATION_ID = 87;
    private static final int AVAILABLE_EXAMS_CHANGES_NOTIFICATION_ID = 88;
    private static final int ENROLLED_EXAMS_CHANGES_NOTIFICATION_ID = 89;

    @Inject
    public NotificationHelper(Context base) {
        super(base);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CHANNEL_BOOKLET_ID = getString(R.string.channel_booklet_id);
        CHANNEL_BOOKLET_NAME = getString(R.string.channel_booklet_name);

        CHANNEL_AVAILABLE_EXAMS_ID = getString(R.string.channel_available_exams_id);
        CHANNEL_AVAILABLE_EXAMS_NAME = getString(R.string.channel_available_exams_name);

        CHANNEL_ENROLLED_EXAMS_ID = getString(R.string.channel_enrolled_exams_id);
        CHANNEL_ENROLLED_EXAMS_NAME = getString(R.string.channel_enrolled_exams_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
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
        notificationManager.createNotificationChannel(channel);

        channel = new NotificationChannel(CHANNEL_AVAILABLE_EXAMS_ID,
                CHANNEL_AVAILABLE_EXAMS_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setShowBadge(true);
        notificationManager.createNotificationChannel(channel);

        channel = new NotificationChannel(CHANNEL_ENROLLED_EXAMS_ID,
                CHANNEL_ENROLLED_EXAMS_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setShowBadge(true);
        notificationManager.createNotificationChannel(channel);
    }


    public NotificationCompat.Builder getNotificationBookletChanges() {
        String title = getString(R.string.channel_booklet_name);
        String content = getString(R.string.channel_booklet_content_changes);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.FRAGMENT_SELECTED_TAG,
                MainActivity.TAG_FRAGMENT_BOOKLET);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                BOOKLET_CHANGES_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return getNotification(CHANNEL_BOOKLET_ID, title, content, pendingIntent);
    }

    public NotificationCompat.Builder getNotificationAvailableChanges() {
        String title = getString(R.string.channel_available_exams_name);
        String content = getString(R.string.channel_available_exams_content_changes);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.FRAGMENT_SELECTED_TAG,
                MainActivity.TAG_FRAGMENT_EXAMS_AVAILABLE);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                AVAILABLE_EXAMS_CHANGES_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return getNotification(CHANNEL_AVAILABLE_EXAMS_ID, title, content, pendingIntent);
    }

    public NotificationCompat.Builder getNotificationEnrolledChanges() {
        String title = getString(R.string.channel_enrolled_exams_name);
        String content = getString(R.string.channel_enrolled_exams_content_changes);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.FRAGMENT_SELECTED_TAG,
                MainActivity.TAG_FRAGMENT_EXAMS_ENROLLED);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                ENROLLED_EXAMS_CHANGES_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return getNotification(CHANNEL_ENROLLED_EXAMS_ID, title, content, pendingIntent);
    }

    @SuppressWarnings("deprecation")
    private NotificationCompat.Builder getNotification(String channelId, String title,
                                                       String content, PendingIntent pendingIntent) {
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
        notificationManager.notify(id, notification.build());
    }
}
