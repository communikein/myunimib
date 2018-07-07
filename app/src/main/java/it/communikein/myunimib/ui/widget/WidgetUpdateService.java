package it.communikein.myunimib.ui.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasServiceInjector;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.ui.MainActivity;

public class WidgetUpdateService extends IntentService implements HasServiceInjector {

    public static final String ACTION_UPDATE_DATA =
            WidgetUpdateService.class.getPackage().getName() +
                    "." + WidgetUpdateService.class.getSimpleName() +
                    "." + "update_app_widget_data";

    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidInjector;

    @Inject
    UnimibRepository repository;


    public WidgetUpdateService() {
        super(WidgetUpdateService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_UPDATE_DATA.equals(action)){
                handleActionUpdateDataView();
            }
        }
    }

    private void handleActionUpdateDataView() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, CareerStatusWidget.class));

        updateAllAppWidget(this, appWidgetManager, appWidgetIds);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.averageMarkTextView);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.cfuTextView);
    }

    public static void startUpdateDataWidgets(Context context) {
        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_UPDATE_DATA);
        context.startService(intent);
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_student_status);

        Intent intent = new Intent(context, MainActivity.class);
        // In widget we are not allowing to use intents as usually. We have to use PendingIntent instead of 'startActivity'
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Here the basic operations the remote view can do.
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        repository.getUser((user) -> {
            String averageScore = "-";
            String totalCfu = "-";

            if (user != null) {
                averageScore = user.printAverageScore();
                totalCfu = user.printTotalCfu();
            }
            views.setTextViewText(R.id.averageMarkTextView, averageScore);
            views.setTextViewText(R.id.cfuTextView, totalCfu);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    public void updateAllAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }



    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidInjector;
    }

}
