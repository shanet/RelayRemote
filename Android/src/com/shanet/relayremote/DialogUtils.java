package com.shanet.relayremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
		// This function displays error messages with a given title and message.
		// It cuts down on duplicate code.
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setIcon(R.drawable.error_icon)
		.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.show();
	}


	public static void showDeleteDialog(final Context context, final Relay relay) {
		// Show the dialog to confirm the deletion of content
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
				((Main)context).reloadRelays();
			}
		})
		.setNegativeButton(R.string.nope, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.show();
	}

	
	public static void showAboutDialog(Context context, int dialogType) {
		createAboutDialog(context, dialogType).show();
	}

	
	public static AlertDialog createAboutDialog(Context context, int dialogType) {
		// Create the given about dialog type
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		View aboutLayout = LayoutInflater.from(context).inflate(R.layout.about_dialog, null);
		TextView aboutText = (TextView) aboutLayout.findViewById(R.id.aboutText);

		dialog.setView(aboutLayout);
		dialog.setIcon(R.drawable.info_icon);
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