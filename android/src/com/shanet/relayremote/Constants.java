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

public abstract class Constants {
    public static final String SETTINGS_FILE = "com.shanet.relay_remote_preferences";
    public static final String LOG_FILE      = "relay_remote_log";
        
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    
    public static final int ADD_EDIT_CODE = 42;
    
    public static final int NETWORK_TIMEOUT = 3000;
        
    public static final int DEFAULT_PORT = 2424;
    public static final int DEFAULT_PIN  = 9;

    public static final char OP_SET     = 's';
    public static final char OP_GET     = 'g';
    public static final char CMD_OFF    = '0';
    public static final char CMD_ON     = '1';
    public static final char CMD_TOGGLE = 't';
    
    public static final int WIDGET_RELAY = 0;
    public static final int WIDGET_GROUP = 1;
    
    public static final int NFC_RELAY = 0;
    public static final int NFC_GROUP = 1;
    
    public static final int ABOUT_THIS_APP = 0;
    public static final int CHANGELOG = 1;
}