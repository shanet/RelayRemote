package com.shanet.relayremote;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

	    // Get all ids
	    ComponentName thisWidget = new ComponentName(context, Widget.class);
	    
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
	    for (int widgetId : allWidgetIds) {
	      // Create some random data
	      int number = (new Random().nextInt(100));

	      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
	      

	      // Register an onClickListener
	      Intent intent = new Intent(context, Widget.class);

	      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

	      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	      //remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
	      appWidgetManager.updateAppWidget(widgetId, remoteViews);
	    }
	  }
}
