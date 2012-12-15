package com.shanet.relayremote;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;

public class Background extends AsyncTask<Bundle, Integer, ArrayList<BasicNameValuePair>> {
	
	private AlertDialog dialog;
	private Context context;
	private Timer timer;
	private TimerTask tt;
	private char op;
	
	public Background(Context context, final char op) {
		this.context  = context;
		this.op       = op;
		
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
		timer.schedule(tt, 500);
	}

	protected void onPostExecute(ArrayList<BasicNameValuePair> states) {
		// Dismiss the dialog and cancel the timer
		dialog.dismiss();
		tt.cancel();
		
		// If the context is an instance of the main activity, update the state of the relays in the listview
		if(op == Constants.OP_GET && (Activity)context instanceof Main) {
			((Main)context).setRelayStates(states);
		}
	}

	protected ArrayList<BasicNameValuePair> doInBackground(Bundle...params) {		
		// There should only be 1 bundle
		if(params.length != 1) {
			((Activity)context).runOnUiThread(new Runnable() {
				public void run() {
					DialogUtils.displayErrorDialog(context, R.string.malformedDataErrorTitle, R.string.malformedDataError);
				}
			});
			return null;
		}
			
		// Get the UI info from the bundle
		Bundle info = params[0];
		op           = info.getChar("op", Constants.OP_GET);
		char cmd     = info.getChar("cmd", Constants.CMD_OFF);
		int pin      = info.getInt("pin", Constants.DEFAULT_PIN);
		String host   = info.getString("server", "");
		int port     = info.getInt("port", Constants.DEFAULT_PORT);

		Server server = new Server(host, port);
		String reply;
		ArrayList<BasicNameValuePair> states = new ArrayList<BasicNameValuePair>();

		try {
			// Connect to the server if not already connected
			if(!server.isConnected()) {
				server.connect();
				
				// Ensure we're connected now
				if(!server.isConnected()) {
					((Activity)context).runOnUiThread(new Runnable() {
						public void run() {
							DialogUtils.displayErrorDialog(context, R.string.serverConnectErrorTitle, R.string.serverConnectError);
						}
					});
					return null;
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
				((Activity)context).runOnUiThread(new Runnable() {
					public void run() {
						DialogUtils.displayErrorDialog(context, R.string.serverErrorTitle, R.string.serverError);
					}
				});
			// If a get operation, format the reply
			} else if(op == Constants.OP_GET) {
				// The first entry in the states list should be the server the states belong to
				states.add(new BasicNameValuePair("server", host));
				
				for(int i=0; i< reply.length(); i+=4) {
					states.add(new BasicNameValuePair(String.valueOf(reply.charAt(i)), String.valueOf((reply.charAt(i+2) == '1') ? Constants.CMD_ON : Constants.CMD_OFF)));
				}
			}
			
			
		} catch (UnknownHostException uhe) {			
			((Activity)context).runOnUiThread(new Runnable() {
				public void run() {
					dialog.dismiss();
					DialogUtils.displayErrorDialog(context, R.string.unknownHostErrorTitle, R.string.unknownHostError);
				}
			});
		} catch (final IOException ioe) {
			((Activity)context).runOnUiThread(new Runnable() {
				public void run() {
					dialog.dismiss();
					DialogUtils.displayErrorDialog(context, context.getString(R.string.serverCommErrorTitle), ioe.getMessage());
				}
			});
		} catch (NullPointerException npe) {
			((Activity)context).runOnUiThread(new Runnable() {
				public void run() {
					dialog.dismiss();
					DialogUtils.displayErrorDialog(context, R.string.serverCommErrorTitle, R.string.serverCommError);
				}
			});
		} finally {
			// Shut down the server
			try {
				if(server != null) server.close();
			} catch (IOException ioe) {}
		}
		
		return states;
	}
}