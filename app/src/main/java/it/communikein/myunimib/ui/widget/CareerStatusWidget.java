package it.communikein.myunimib.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

/**
 * Implementation of App Widget functionality.
 */
public class CareerStatusWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetUpdateService.startUpdateDataWidgets(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {}

}