package com.shanet.relayremote;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
          Widget.setState(appWidgetId, (((String)states.get(i).second).charAt(0) == Constants.CMD_ON) ? Widget.STATE_ON : Widget.STATE_OFF);

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

    // Create the server
    Server server;
    String reply;
    ArrayList<Pair> states = new ArrayList<Pair>();

    try {
      server = new Server(host, port);
    } catch (SocketException e) {
      if(!isWidget) {
        ((Activity)context).runOnUiThread(new Runnable() {
          public void run() {
            dialog.dismiss();
            DialogUtils.displayErrorDialog(context, R.string.serverCommErrorTitle, R.string.serverCommError);
          }
        });
      }
      return createErrorStatesArray(host, pin, cmd);
    }

    try {
      // Connect to the server if not already connected
      if(!server.isConnected()) {
        server.connect();

        // Ensure we're connected now
        if(!server.isConnected()) {
          if(!isWidget) {
            ((Activity)context).runOnUiThread(new Runnable() {
              public void run() {
                DialogUtils.displayErrorDialog(context, R.string.serverConnectErrorTitle, R.string.serverConnectError);
              }
            });
          }
          throw new Exception();
        }
      }

      // Format and send the data to the server
      if(op == Constants.OP_GET) {
        // GET operations are just the letter "g"
        server.send(Constants.OP_GET + "\n");
      } else {
        // SET operations are of the form "s-[pin]-[state]"
        // Pin is the pin the relay is connected to
        // State is 0 for off, 1 for on, or t for toggle (not used in this app)
        server.send(Constants.OP_SET + "-" + pin + "-" + cmd + "\n");
      }

      // Get the reply from the server
      reply = server.receive();

      if(reply.equals("ERR")) {
        if(!isWidget) {
          ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
              DialogUtils.displayErrorDialog(context, R.string.serverErrorTitle, R.string.serverError);
            }
          });
        }
        throw new Exception();
       // Create the states array
      } else {
        // The first entry in the states list should be the server the states belong to
        states.add(new Pair("server", host));

        // If a get operation, format the reply
        if(op == Constants.OP_GET) {
          for(int i=0; i< reply.length(); i+=4) {
            states.add(new Pair(String.valueOf(reply.charAt(i)), String.valueOf((reply.charAt(i+2) == '1') ? Constants.CMD_ON : Constants.CMD_OFF)));
          }
        // Else, it's a set command so just add the pin we just handled
        } else {
          states.add(new Pair(String.valueOf(pin), String.valueOf(cmd)));
        }
      }


    } catch (UnknownHostException uhe) {
      states = createErrorStatesArray(host, pin, cmd);
      if(!isWidget) {
        ((Activity)context).runOnUiThread(new Runnable() {
          public void run() {
            dialog.dismiss();
            DialogUtils.displayErrorDialog(context, R.string.unknownHostErrorTitle, R.string.unknownHostError);
          }
        });
      }
    } catch (final IOException ioe) {
      states = createErrorStatesArray(host, pin, cmd);
      if(!isWidget) {
        ((Activity)context).runOnUiThread(new Runnable() {
          public void run() {
            dialog.dismiss();
            DialogUtils.displayErrorDialog(context, context.getString(R.string.serverCommErrorTitle), ioe.getMessage());
          }
        });
      }
    } catch (NullPointerException npe) {
      states = createErrorStatesArray(host, pin, cmd);
      if(!isWidget) {
        ((Activity)context).runOnUiThread(new Runnable() {
          public void run() {
            dialog.dismiss();
            DialogUtils.displayErrorDialog(context, R.string.serverCommErrorTitle, R.string.serverCommError);
          }
        });
      }
    } catch (Exception e) {
      states = createErrorStatesArray(host, pin, cmd);
    } finally {
      // Shut down the server
      try {
        if(server != null && !server.isConnected()) server.close();
      } catch (IOException ioe) {}
    }

    return states;
  }

  private ArrayList<Pair> createErrorStatesArray(String host, int pin, char cmd) {
    // If there was an error, the state should fall back to whatever the original state was
    ArrayList<Pair> states = new ArrayList<Pair>();
    states.add(new Pair("server", host));
    states.add(new Pair(String.valueOf(pin), String.valueOf((cmd == Constants.CMD_OFF ? Constants.CMD_ON : Constants.CMD_OFF))));
    return states;
  }
}
