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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public abstract class DialogUtils {

	public static void displayErrorDialog(Context context, int title, int message) {
		displayErrorDialog(context, context.getString(title), context.getString(message));
	}
	
	
	public static void displayErrorDialog(Context context, String title, String message) {
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setIcon(R.drawable.error_icon)
		.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.show();
	}
	

	public static void displayInfoDialog(Context context, int title, int message) {
		displayInfoDialog(context, context.getString(title), context.getString(message));
	}
	
	
	public static void displayInfoDialog(Context context, String title, String message) {
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setIcon(R.drawable.about_icon)
		.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.show();
	}
	

	public static void showDeleteDialog(final Context context, final Relay relay) {
		// Show the dialog to confirm the deletion of relay
		new AlertDialog.Builder(context)
		.setTitle(R.string.deleteRelayTitle)
		.setMessage(String.format(context.getString(R.string.deleteDialog), relay.getName()))
		.setIcon(R.drawable.error_icon)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Delete the relay from the db
				new Database(context).deleteRelay(relay);
				Toast.makeText(context, R.string.deletedRelay, Toast.LENGTH_SHORT).show();
				
				// Tell the main activity to reload the relays
				((Main)context).getRelaysFrag().reloadRelays();
			}
		})
		.setNegativeButton(R.string.nope, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.show();
	}
	
	
	public static void showDeleteDialog(final Context context, final RelayGroup group) {
		new AlertDialog.Builder(context)
		.setTitle(R.string.deleteRelayTitle)
		.setMessage(String.format(context.getString(R.string.deleteDialog), group.getName()))
		.setIcon(R.drawable.error_icon)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Delete the relay from the db
				new Database(context).deleteRelayGroup(group);
				Toast.makeText(context, R.string.deletedGroup, Toast.LENGTH_SHORT).show();
				
				// Tell the main activity to reload the relays
				((Main)context).getRelayGroupsFrag().reloadGroups();
			}
		})
		.setNegativeButton(R.string.nope, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.show();
	}
	
	
	public static void displayNfcTypeDialog(final Context context, final int relay_type, final int id) {
		new AlertDialog.Builder(context)
		.setTitle(R.string.createNFC)
		.setMessage(R.string.aboutNFC)
		.setIcon(R.drawable.about_icon)
		.setPositiveButton(R.string.gotIt, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Show the command type picker
				new AlertDialog.Builder(context)
				.setTitle(R.string.createNFC)
				.setIcon(R.drawable.about_icon)
				.setItems(R.array.nfcCmdOptions, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Determine what command the user selected
						char cmd = Constants.CMD_ON;
						switch(which) {
		            	   case 0:
		            		   cmd = Constants.CMD_ON;
		            		   break;
		            	 	case 1:
		            	 		cmd = Constants.CMD_OFF;
		            	 		break;
		            	   	case 2:
		            	   		cmd = Constants.CMD_TOGGLE;	   	
						}
						
		        	    // Construct the data to write to the tag
		        	    // Should be of the form [relay/group]-[rid/gid]-[cmd]
		        	    String nfcMessage = relay_type + "-" + id + "-" + cmd;
		        	    
		        	    // When an NFC tag comes into range, call the main activity which handles writing the data to the tag
		        	    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
					
		   				Intent nfcIntent = new Intent(context, Main.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		   				nfcIntent.putExtra("nfcMessage", nfcMessage);
		   				PendingIntent pi = PendingIntent.getActivity(context, 0, nfcIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		   				IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);	
		   				
		   				nfcAdapter.enableForegroundDispatch((Activity)context, pi, new IntentFilter[] {tagDetected}, null);
		               }
				})
				.show();
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.show();
	}

	
	public static void displayAboutDialog(Context context, int dialogType) {
		createAboutDialog(context, dialogType).show();
	}

	
	public static AlertDialog createAboutDialog(Context context, int dialogType) {
		// Create the given about dialog type
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		View aboutLayout = LayoutInflater.from(context).inflate(R.layout.about_dialog, null);
		TextView aboutText = (TextView) aboutLayout.findViewById(R.id.aboutText);

		dialog.setView(aboutLayout);
		dialog.setIcon(R.drawable.about_icon);
		//((ImageView)aboutLayout.findViewById(R.id.aboutLogo)).setImageDrawable(context.getResources().getDrawable(R.drawable.full_logo));

		dialog.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});

		// Determine what dialog type is to be shown
		switch(dialogType) {
		case Constants.ABOUT_THIS_APP:
			dialog.setTitle(R.string.aboutThisAppTitle);
			aboutText.setText(R.string.aboutThisAppText);
			break;
		case Constants.CHANGELOG:
			dialog.setTitle(R.string.changelogTitle);
			aboutText.setText(R.string.changelogText);
			break;
		default:
			return null;
		}

		// Make links out all URL's and email's in the dialog
		// Except the release notes. The linker recognizes
		// 4 digit version numbers as IP addresses
		if(dialogType != Constants.CHANGELOG)
			Linkify.addLinks(aboutText, Linkify.ALL);

		return dialog.create();
	}
}