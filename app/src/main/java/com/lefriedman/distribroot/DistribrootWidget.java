package com.lefriedman.distribroot;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.lefriedman.distribroot.services.WidgetService;
import com.lefriedman.distribroot.ui.FindDistributionActivity;

import java.util.Map;

import static com.lefriedman.distribroot.util.Constants.WIDGET_ACTION;

/**
 * Implementation of App Widget functionality.
 */
public class DistribrootWidget extends AppWidgetProvider {
    private static final String TAG = DistribrootWidget.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //Set a view to display initial message if no distributions subscribed
        String initialMessage = context.getString(R.string.appwidget_no_text);

        //iterate through preferences and display the distributions user is subscribed to
        String subscribedDistributors = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            Map<String, ?> savedPrefs = sharedPreferences.getAll();
            for (Map.Entry<String, ?> pref : savedPrefs.entrySet()){
                if (pref.getValue().equals(true)){
                    subscribedDistributors += pref.getKey() + "\n";
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.distribroot_widget);
        views.setTextViewText(R.id.appwidget_subscribe_msg, initialMessage);
        // Hide initial message once distributors are subscribed to
        if (!subscribedDistributors.isEmpty()){
            views.setViewVisibility(R.id.appwidget_subscribe_msg, View.INVISIBLE);
        }
        views.setTextViewText(R.id.appwidget_text, subscribedDistributors);

        //Create the intent to launch findDistribution activity initial message is clicked
        Intent clickIntent = new Intent(context, FindDistributionActivity.class);
        PendingIntent launchActivity = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_subscribe_msg,launchActivity);

        Intent serviceActionIntent = new Intent(context, WidgetService.class);
        serviceActionIntent.setAction(WIDGET_ACTION);
        PendingIntent actionPendingIntent = PendingIntent.getService(context, 0, serviceActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_text, actionPendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
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

