package com.shanet.relayremote;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Pair;
import android.widget.RemoteViews;

public class Background extends AsyncTask<Bundle, Integer, ArrayList<Pair>> {
  private AlertDialog dialog;
  private Context context;
  private Timer timer;
  private TimerTask tt;

  private char op;
  private int pin;

  private boolean isWidget;
  private int appWidgetId;

  public Background(Context context, final char op, boolean isWidget) {
  	this.context  = context;
  	this.op       = op;
    this.isWidget = isWidget;

    // If a widget, don't show any dialogs
    if(isWidget) return;

    dialog = new ProgressDialog(context);

    // Show a dialog if the bg thread runs longer than the time specified in onPreExecute() below
    timer = new Timer();
    tt = new TimerTask() {
      @Override
      public void run() {
        Looper.prepare();
        ((Activity)Background.this.context).runOnUiThread(new Runnable() {
          public void run() {
            dialog = ProgressDialog.show(Background.this.context, "",
             Background.this.context.getString((op == Constants.OP_SET) ? R.string.connServer : R.string.gettingStates));
          }
        });
      }
    };
  }

  protected void onPreExecute() {
    // Show the dialog after 500ms
    if(!isWidget) timer.schedule(tt, 500);
  }

  protected void onPostExecute(ArrayList<Pair> states) {
    // Dismiss the dialog and cancel the timer
    if(!isWidget) {
      dialog.dismiss();
      tt.cancel();
    }

    // If the context is an instance of the main activity, update the state of the relays in the listview
    if(!isWidget && (Activity)context instanceof Main && states.size() > 1) {
      ((Main)context).setRelaysAndGroupsStates(states);
    } else if(isWidget) {
      // If a widget, update the states map and widget UI
      for(int i=1; i<states.size(); i++) {
        if(pin == Integer.valueOf((String)states.get(i).first)) {
          // Set the state of the widget in the widget class
          Widget.setState(appWidgetId, ((String)states.get(i).second).charAt(0) == Constants.CMD_ON ? Widget.STATE_ON : Widget.STATE_OFF);

          RemoteViews views = Widget.getWidgetViews(context, appWidgetId);
          AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
        }
      }
    }
  }

  protected ArrayList<Pair> doInBackground(Bundle...params) {
    // There should only be 1 bundle
    if(params.length != 1) {
      if(!isWidget) {
        ((Activity)context).runOnUiThread(new Runnable() {
          public void run() {
            DialogUtils.displayErrorDialog(context, R.string.malformedDataErrorTitle, R.string.malformedDataError);
          }
        });
      }
      return new ArrayList<Pair>();
    }

    // Get the UI info from the bundle
    Bundle info = params[0];
    op          = info.getChar("op", Constants.OP_GET);
    char cmd    = info.getChar("cmd", Constants.CMD_OFF);
    pin         = info.getInt("pin", Constants.DEFAULT_PIN);
    String host = info.getString("server", "");
    int port    = info.getInt("port", Constants.DEFAULT_PORT);
    appWidgetId = info.getInt("appWidgetId", -1);

    Server server = new Server(host, port);
    ArrayList<Pair> states = new ArrayList<Pair>();

    // The first entry in the states list should be the server the states belong to
    states.add(new Pair("server", host));

    try{
      if(op == Constants.OP_SET) {
        server.post("/api/pins/" + pin, "state=" + command(cmd));
        states.add(new Pair(String.valueOf(pin), String.valueOf(cmd)));
      } else if(op == Constants.OP_GET) {
        String response = server.get("/api/pins");
        JSONObject jsonResponse = new JSONObject(response);
        Iterator<String> keys = jsonResponse.keys();

        while(keys.hasNext()) {
          String pin = keys.next();
          Integer state = (Integer)jsonResponse.get(pin);

          states.add(new Pair(pin, String.valueOf(state == 1 ? Constants.CMD_ON : Constants.CMD_OFF)));
        }
      }
    } catch(IOException | JSONException exception) {
      if(!isWidget) {
        ((Activity)context).runOnUiThread(new Runnable() {
          public void run() {
            dialog.dismiss();
            DialogUtils.displayErrorDialog(context, context.getString(R.string.serverCommErrorTitle), exception.getMessage());
          }
        });
      }

      return createErrorStatesArray(host, pin, cmd);
    }

    return states;
  }

  private ArrayList<Pair> createErrorStatesArray(String host, int pin, char cmd) {
    // If there was an error, the state should fall back to whatever the original state was
    ArrayList<Pair> states = new ArrayList<Pair>();
    states.add(new Pair("server", host));
    states.add(new Pair(String.valueOf(pin), String.valueOf(cmd == Constants.CMD_OFF ? Constants.CMD_ON : Constants.CMD_OFF)));
    return states;
  }

  private String command(char cmd) {
    switch(cmd) {
      case Constants.CMD_OFF:
        return "off";
      case Constants.CMD_ON:
        return "on";
      case Constants.CMD_TOGGLE:
        return "toggle";
    }

    return null;
  }
}
