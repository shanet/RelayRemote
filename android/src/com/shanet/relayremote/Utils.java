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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.view.MenuItem;

public abstract class Utils {	
	
    public static boolean onOptionsItemSelected(Context context, MenuItem item) {
    	switch(item.getItemId()) {
			case R.id.addRelayMenuOption:
				((Activity)context).startActivityForResult(new Intent(context, AddRelay.class), Constants.ADD_EDIT_CODE);
				return true;
				
			case R.id.createGroupMenuOption:
				((Activity)context).startActivityForResult(new Intent(context, AddRelayGroup.class), Constants.ADD_EDIT_CODE);
				return true;
				
    		case R.id.refreshRelaysMenu:
    		case R.id.refreshGroupsMenu:
    			((Main)context).updateRelaysAndGroupsStates(false);
    			return true;
    			    			
    		case R.id.allOnMenuOption:
    			((Main)context).getRelaysFrag().turnOnOffAllRelays(Constants.CMD_ON);
    			return true;
    			
    		case R.id.groupAllOnMenuOption:
    			((Main)context).getRelayGroupsFrag().turnOnOffAllGroups(Constants.CMD_ON);
    			return true;
    			
    		case R.id.allOffMenuOption:
    			((Main)context).getRelaysFrag().turnOnOffAllRelays(Constants.CMD_OFF);
    			return true;
    			
    		case R.id.groupAllOffMenuOption:
    			((Main)context).getRelayGroupsFrag().turnOnOffAllGroups(Constants.CMD_OFF);
    			return true;
    			
    		case R.id.aboutMenuOption:
    			DialogUtils.displayAboutDialog(context, Constants.ABOUT_THIS_APP);
    			return true;
    			
    		case R.id.changelogMenuOption:
    			DialogUtils.displayAboutDialog(context, Constants.CHANGELOG);
    			return true;
    	}
    	
		return false;
    }
    
    
    public static void startNetworkThreadForRelay(Context context, Relay relay, char cmd) {
    	// Create the background info bundle
		Bundle bgInfo = new Bundle();
		bgInfo.putChar("op", Constants.OP_SET);
		bgInfo.putString("server", relay.getServer());
		bgInfo.putInt("port", relay.getPort());
		bgInfo.putInt("pin", relay.getPin());
		bgInfo.putChar("cmd", cmd);
		
		// Call the bg thread to send the commands to the server
		new Background(context, Constants.OP_SET, false).execute(bgInfo);
    }
    
    
    public static boolean hasNfcSupport(Context context) {
		// Check if the device has NFC and if it is enabled
		NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
		NfcAdapter adapter = manager.getDefaultAdapter();
		if(adapter == null) {
			DialogUtils.displayErrorDialog(context, R.string.nfcNotSupportedErrorTitle, R.string.nfcNotSupportedError);
			return false;
		} else if(!adapter.isEnabled()) {
			DialogUtils.displayErrorDialog(context, R.string.nfcDisabledErrorTitle, R.string.nfcDisabledError);
			return false;
		}
		
		return true;
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
					VersionUtils.writeVersionCode(context);
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
