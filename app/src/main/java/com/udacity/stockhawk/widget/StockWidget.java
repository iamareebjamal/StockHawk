package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

public class StockWidget extends AppWidgetProvider {
    private static final String REFRESH_ACTION = "com.udacity.stockhawk.appwidget.action.REFRESH";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
        views.setRemoteAdapter(R.id.list_view, new Intent(context, StockWidgetService.class));

        Intent launch = new Intent(context, DetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launch, 0);
        views.setPendingIntentTemplate(R.id.list_view, pendingIntent);

        launch = new Intent(context, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, launch, 0);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(REFRESH_ACTION);
        intent.setComponent(new ComponentName(context, StockWidget.class));
        context.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (REFRESH_ACTION.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, StockWidget.class);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.list_view);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

