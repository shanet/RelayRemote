package com.shanet.relayremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public abstract class VersionUtils {
	 
	public static void writeOnFirstRun(Context context) {
		// Write a pref for if the app has been run before and default the port
		SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();
		editor.putBoolean("onFirstRun", false);
		editor.putInt("port", Constants.DEFAULT_PORT);
		editor.commit();
	}

	public static void writeVersionCode(Context context) {
		// Update the version code stored in the prefs
		SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();
		try {
			editor.putInt("versionCode", context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA).versionCode);
		} catch (NameNotFoundException e) {}
		editor.commit();
	}

	public static boolean compareVersionCode(Context context) {
		// Determines if the app has been updated
		try {
			if(context.getSharedPreferences(Constants.SETTINGS_FILE, 0).getInt("versionCode", 0) != context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA).versionCode)
				return true;
			else
				return false;
		} catch (NameNotFoundException e) {}
		return false;
	}
}
