package com.lefriedman.distribroot.services;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lefriedman.distribroot.DistribrootWidget;
import com.lefriedman.distribroot.R;

import java.util.Map;

import static com.lefriedman.distribroot.util.Constants.WIDGET_ACTION;

public class WidgetService extends IntentService {

    private static final String TAG = WidgetService.class.getSimpleName();


    public WidgetService() {
        super("WidgetService");
    }

    public static void startAction(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(WIDGET_ACTION);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (WIDGET_ACTION.equals(action)) {
                handleAction();
            }
        }

    }

    private void handleAction() {
        Log.d(TAG, "handleAction: called");
    }


}
