package com.shanet.relayremote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
  public static final int STATE_UNKNOWN = 0;
  public static final int STATE_OFF     = 1;
  public static final int STATE_ON      = 2;

  private static SparseIntArray states = new SparseIntArray();

  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for(int appWidgetId : appWidgetIds) {
      try {
        // Get the name of relay/group assigned to this widget
        Database db = new Database(context);
        Bundle widgetInfo = db.selectWidget(appWidgetId);

        if(widgetInfo == null) continue;

        switch(widgetInfo.getInt("type")) {
          case Constants.WIDGET_RELAY:
            // Call the bg thread to get the state from the server
            Relay relay = db.selectRelay(widgetInfo.getInt("id"));
            new Background(context, Constants.OP_GET, true).execute(getBgInfoBundle(relay, Constants.OP_GET, appWidgetId));
            break;
          case Constants.WIDGET_GROUP:
            RelayGroup group = db.selectRelayGroup(widgetInfo.getInt("id"));

            for(int rid : group.getRids()) {
              // Call the bg thread to get the state from the server
              new Background(context, Constants.OP_GET, true).execute(getBgInfoBundle(db.selectRelay(rid), Constants.OP_GET, appWidgetId));
            }
        }

        // Update the UI
        RemoteViews views = getWidgetViews(context, appWidgetId);
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
      } catch (NullPointerException npe) {
        continue;
      }
    }
  }

  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);

    if(intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
      Uri data = intent.getData();
      int appWidgetId = Integer.parseInt(data.getSchemeSpecificPart());

      Database db = new Database(context);

      // Get the type and id of the relay associated with this widget from the db
      Bundle widgetInfo = db.selectWidget(appWidgetId);
      int id = widgetInfo.getInt("id");

      switch(widgetInfo.getInt("type")) {
        case Constants.WIDGET_RELAY:
          // Call the bg thread to send the commands to the server
          new Background(context, Constants.OP_SET, true).execute(getBgInfoBundle(db.selectRelay(id), Constants.OP_SET, appWidgetId));
          break;

        case Constants.WIDGET_GROUP:
          RelayGroup group = db.selectRelayGroup(id);

          for(int rid : group.getRids()) {
            // Call the bg thread to send the commands to the server
            new Background(context, Constants.OP_SET, true).execute(getBgInfoBundle(db.selectRelay(rid), Constants.OP_SET, appWidgetId));
          }
      }

      // Update the UI with the new unknown state until the bg thread completes
      setState(appWidgetId, STATE_UNKNOWN);
      RemoteViews views = getWidgetViews(context, appWidgetId);
      AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }
  }

  public Bundle getBgInfoBundle(Relay relay, char op, int appWidgetId) {
    Bundle bgInfo = new Bundle();

    bgInfo.putChar("op", op);
    bgInfo.putString("server", relay.getServer());
    bgInfo.putInt("port", relay.getPort());
    bgInfo.putInt("pin", relay.getPin());
    bgInfo.putChar("cmd", (getState(appWidgetId) == STATE_OFF ? Constants.CMD_ON : Constants.CMD_OFF));
    bgInfo.putInt("appWidgetId", appWidgetId);

    return bgInfo;
  }

  public static void setState(int appWidgetId, int state) {
    states.put(appWidgetId, state);
  }

  public static int getState(int appWidgetId) {
    return states.get(appWidgetId);
  }

  public static RemoteViews getWidgetViews(Context context, int appWidgetId) {
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

    // Set the appropriate name on the label
    views.setTextViewText(R.id.widgetName, getName(context, appWidgetId));

    // Set the on click handler for the widget
    Intent intent = new Intent(context, Widget.class);
    intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
    intent.setData(Uri.parse(Integer.valueOf(appWidgetId).toString()));
    views.setOnClickPendingIntent(R.id.widgetButton, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE));

    // Set the indicator to whatever the current state of the widget is
    switch(getState(appWidgetId)) {
      default:
      case STATE_UNKNOWN:
        views.setImageViewResource(R.id.widgetIndicator, R.drawable.widget_half);
        break;
      case STATE_ON:
        views.setImageViewResource(R.id.widgetIndicator, R.drawable.widget_on);
        break;
      case STATE_OFF:
        views.setImageViewResource(R.id.widgetIndicator, R.drawable.widget_off);
        break;
    }

    return views;
  }

  private static String getName(Context context, int appWidgetId) {
    Database db = new Database(context);
    Bundle widgetInfo = db.selectWidget(appWidgetId);

    if(widgetInfo == null) return "";

    switch(widgetInfo.getInt("type")) {
      case Constants.WIDGET_RELAY:
        Relay relay = db.selectRelay(widgetInfo.getInt("id"));
        return relay.getName();

      case Constants.WIDGET_GROUP:
        RelayGroup group = db.selectRelayGroup(widgetInfo.getInt("id"));
        return group.getName();
    }

    return "";
  }

  public void onDeleted(Context context, int[] appWidgetIds) {
    super.onDeleted(context, appWidgetIds);

    // Delete the widgets from the db
    Database db = new Database(context);

    for(int appWidgetId : appWidgetIds) {
      db.deleteWidget(appWidgetId);
    }
  }
}
