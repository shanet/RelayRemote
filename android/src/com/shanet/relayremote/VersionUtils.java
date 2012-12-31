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
