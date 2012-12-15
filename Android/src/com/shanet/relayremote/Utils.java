package com.shanet.relayremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;

public abstract class Utils {	
	
    public static boolean onOptionsItemSelected(Context context, MenuItem item) {
    	switch(item.getItemId()) {
    		case R.id.refreshRelaysMenu:
    			((Main)context).updateRelayStates();
    			return true;
    		case R.id.allOnMenuOption:
    			((Main)context).turnOnOffAllRelays(Constants.CMD_ON);
    			return true;
    		case R.id.allOffMenuOption:
    			((Main)context).turnOnOffAllRelays(Constants.CMD_OFF);
    			return true;
    		case R.id.addRelayMenu:
    			((Activity)context).startActivityForResult(new Intent(context, AddRelay.class), Constants.ADD_EDIT_CODE);
    			return true;
    		case R.id.aboutMenu:
    			DialogUtils.showAboutDialog(context, Constants.ABOUT_THIS_APP);
    			return true;
    		case R.id.changelogMenu:
    			DialogUtils.showAboutDialog(context, Constants.CHANGELOG);
    			return true;		
    	}
    	
		return false;
    }
    
    
	public static void writeIntPref(Context context, String key, int data) {
		SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();
		editor.putInt(key, data);
		editor.commit();
	}
	
	
	public static int getIntPref(Context context, String key) {
		return context.getSharedPreferences(Constants.SETTINGS_FILE, 0).getInt(key, -1);
	}
	
	public static void showOpeningDialogs(final Context context) {
        // Checking if this is the first run and if it is, write the corresponding values to the prefs.
        if(context.getSharedPreferences(Constants.SETTINGS_FILE, 0).getBoolean("onFirstRun", true)) {			
          	AlertDialog welcomeDialog = DialogUtils.createAboutDialog(context, Constants.ABOUT_THIS_APP);
          	welcomeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					VersionUtils.writeOnFirstRun(context);
				}
			});
          	welcomeDialog.setTitle(R.string.welcomeTitle);
          	welcomeDialog.show();
			
        // Display the release notes if this is a new version
        } else if(VersionUtils.compareVersionCode(context)) {
          	AlertDialog changelogDialog = DialogUtils.createAboutDialog(context, Constants.CHANGELOG);
          	changelogDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					VersionUtils.writeVersionCode(context);
				}
			});
          	changelogDialog.setTitle(R.string.changelogTitle);
          	changelogDialog.show();
        }
	}
}
