// Copyright (C) 2012 Shane Tully
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        for(int appWidgetId : appWidgetIds) {
            try {
                // Get the name of relay/group assigned to this widget
                Database db = new Database(context);
                Bundle widgetInfo = db.selectWidget(appWidgetId);

                if(widgetInfo == null) continue;

                String name = "";
                switch(widgetInfo.getInt("type")) {
                    case Constants.WIDGET_RELAY:
                        Relay relay = db.selectRelay(widgetInfo.getInt("id"));

                        name = relay.getName();

                        // Call the bg thread to get the state from the server
                        new Background(context, Constants.OP_GET, true).execute(getBgInfoBundle(relay, Constants.OP_GET, appWidgetId));

                        break;
                    case Constants.WIDGET_GROUP:
                        RelayGroup group = db.selectRelayGroup(widgetInfo.getInt("id"));

                        name = group.getName();

                        for(int rid : group.getRids()) {
                            // Call the bg thread to get the state from the server
                            new Background(context, Constants.OP_GET, true).execute(getBgInfoBundle(db.selectRelay(rid), Constants.OP_GET, appWidgetId));
                        }
                }

                // Set the name of the relay/group the widget is for
                views.setTextViewText(R.id.widgetName, name);

                // Check if the state is already in the states array
                // If so, set the indicator as such; if not (unknown state), set it as half while the background thread gets the state
                switch(getState(appWidgetId)) {
                    case STATE_UNKNOWN:
                        views.setImageViewResource(R.id.widgetIndicator, R.drawable.widget_half);
                    case STATE_ON:
                        views.setImageViewResource(R.id.widgetIndicator, R.drawable.widget_on);
                    case STATE_OFF:
                        views.setImageViewResource(R.id.widgetIndicator, R.drawable.widget_off);
                }

                // Set a pending intent on the button for button clicks
                Intent intent = new Intent(context, Widget.class);
                intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                intent.setData(Uri.parse(Integer.valueOf(appWidgetId).toString()));
                views.setOnClickPendingIntent(R.id.widgetButton, PendingIntent.getBroadcast(context, 0, intent, 0));

                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }


    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
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

            // Update the indicator image
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setImageViewResource(R.id.widgetIndicator, R.drawable.widget_half);
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


    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // Delete the widgets from the db
        Database db = new Database(context);

        for(int appWidgetId : appWidgetIds) {
            db.deleteWidget(appWidgetId);
        }
    }
}
