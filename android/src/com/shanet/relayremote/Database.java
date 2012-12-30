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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

public class Database extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "RelayRemote";
	private static final int DATABASE_VERSION = 5;
	
	private static final String TABLE_RELAYS  = "relays";
	private static final String TABLE_GROUPS  = "groups";
	private static final String TABLE_WIDGETS = "widgets";

	private static final String FIELD_RELAYS_KEY    = "rid";
	private static final String FIELD_RELAYS_GROUP  = "gid";
	private static final String FIELD_RELAYS_NAME   = "name";
	private static final String FIELD_RELAYS_PIN    = "pin";
	private static final String FIELD_RELAYS_SERVER = "server";
	private static final String FIELD_RELAYS_PORT   = "port";
	
	private static final String FIELD_GROUPS_KEY    = "gid";
	private static final String FIELD_GROUPS_NAME   = "name";
	
	private static final String FIELD_WIDGETS_KEY   = "wid";
	private static final String FIELD_WIDGETS_TYPE  = "type";
	private static final String FIELD_WIDGETS_ID    = "id";
		
	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	
	public void onCreate(SQLiteDatabase db) {
		// Create the relays table
		db.execSQL("CREATE TABLE " + TABLE_RELAYS + "(" + 
	               FIELD_RELAYS_KEY    + " INTEGER PRIMARY KEY, " +
				   FIELD_RELAYS_GROUP  + " INTEGER, " +
				   FIELD_RELAYS_NAME   + " TEXT, " +
				   FIELD_RELAYS_PIN    + " INTEGER, " +
	               FIELD_RELAYS_SERVER + " TEXT, " +
				   FIELD_RELAYS_PORT   + " INTEGER, " +
				   "FOREIGN KEY(" + FIELD_RELAYS_GROUP + ") REFERENCES " + TABLE_GROUPS + "(" + FIELD_GROUPS_KEY + "))");
		
		// Create the groups table
		db.execSQL("CREATE TABLE " + TABLE_GROUPS + "(" + 
	               FIELD_GROUPS_KEY    + " INTEGER PRIMARY KEY, " +
				   FIELD_GROUPS_NAME   + " TEXT)");
		
		// Create the widgets table
		db.execSQL("CREATE TABLE " + TABLE_WIDGETS + "(" + 
	               FIELD_WIDGETS_KEY   + " INTEGER PRIMARY KEY, " +
				   FIELD_WIDGETS_TYPE  + " INTEGER, " +
				   FIELD_WIDGETS_ID    + " INTEGER)");
	}
	
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop the old tables
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RELAYS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIDGETS);
		
		// Create new tables
		onCreate(db);
	}
	
	
	public Relay selectRelay(int rid) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for the given relay with rid
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RELAYS + " WHERE " + FIELD_RELAYS_KEY + " = " + rid, null);
		
		// Go to the first result or result null if it doesn't exist
		if(!cursor.moveToFirst()) {
			db.close();
			return null;
		}
		
		// Create a new relay object and return it
		Relay relay = new Relay(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getInt(5), false);
		
		db.close();
		return relay;
	}
	
	
	public ArrayList<Relay> selectAllRelays() {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for all relays
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RELAYS, null);
		
		// Add each result to an ArrayList
		ArrayList<Relay> relays = new ArrayList<Relay>();
		
		// If no results, just close the connection and return an empty list
		if(!cursor.moveToFirst()) {
			db.close();
			return relays;
		}
		
		do{
			relays.add(new Relay(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getInt(5), false));
		} while(cursor.moveToNext());
		
		db.close();
		return relays;
	}
	
	
	public int insertRelay(Relay relay) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Get info from the relay
		ContentValues values = new ContentValues();
		values.put(FIELD_RELAYS_NAME, relay.getName());
		values.put(FIELD_RELAYS_GROUP, relay.getGid());
		values.put(FIELD_RELAYS_PIN, relay.getPin());
		values.put(FIELD_RELAYS_SERVER, relay.getServer());
		values.put(FIELD_RELAYS_PORT, relay.getPort());
		
		// Insert the values into the db
		int rid = (int)db.insert(TABLE_RELAYS, null, values);
		db.close();
		
		return rid;
	}
	
	
	public int updateRelay(Relay relay) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that the relay has a valid rid
		if(relay.getRid() < 0) return Constants.FAILURE;
		
		// Get info from the relay
		ContentValues values = new ContentValues();
		values.put(FIELD_RELAYS_NAME, relay.getName());
		values.put(FIELD_RELAYS_GROUP, relay.getGid());
		values.put(FIELD_RELAYS_PIN, relay.getPin());
		values.put(FIELD_RELAYS_SERVER, relay.getServer());
		values.put(FIELD_RELAYS_PORT, relay.getPort());
		
		db.update(TABLE_RELAYS, values, FIELD_RELAYS_KEY + " = " + relay.getRid(), null);
		db.close();
		
		return Constants.SUCCESS;
	}
	
	
	public int deleteRelay(Relay relay) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that the relay has a valid rid
		if(relay.getRid() < 0) return Constants.FAILURE;
		
		db.delete(TABLE_RELAYS, FIELD_RELAYS_KEY + " = " + relay.getRid(), null);
		db.close();
		
		return Constants.SUCCESS;
	}
	
	
	
	
	public RelayGroup selectRelayGroup(int gid) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for the given group ID
		Cursor cursor = db.rawQuery("SELECT " + FIELD_GROUPS_NAME + " FROM " + TABLE_GROUPS + " WHERE " + FIELD_GROUPS_KEY + " = " + gid, null);
		
		// Go to the first result or result null if it doesn't exist
		if(!cursor.moveToFirst()) {
			db.close();
			return null;
		}
		
		String name = cursor.getString(0);
		
		// Get the relays in this group
		cursor = db.rawQuery("SELECT " + FIELD_RELAYS_KEY + " FROM " + TABLE_RELAYS + " WHERE " + FIELD_GROUPS_KEY + " = " + gid, null);
		
		// Go to the first result or result null if it doesn't exist
		if(!cursor.moveToFirst()) {
			db.close();
			return null;
		}
		
		// Construct the list of relays in the group
		ArrayList<Integer> rids = new ArrayList<Integer>();
		do {
			rids.add(cursor.getInt(0));
		} while(cursor.moveToNext());
		
		// Create a new relay object and return it
		RelayGroup group = new RelayGroup(gid, name, rids, false);
		
		db.close();
		return group;
	}
	
	
	public ArrayList<RelayGroup> selectAllRelayGroups() {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for all groups
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GROUPS, null);
		
		// Add each result to an ArrayList
		ArrayList<RelayGroup> groups = new ArrayList<RelayGroup>();
		
		// If no results, just close the connection and return an empty list
		if(!cursor.moveToFirst()) {
			db.close();
			return groups;
		}
		
		// Add each group to the groups list and construct the list of relays in the group
		ArrayList<Integer> rids;
		Cursor relayCursor;
		do {
			rids = new ArrayList<Integer>();
			relayCursor = db.rawQuery("SELECT " + FIELD_RELAYS_KEY + " FROM " + TABLE_RELAYS + " WHERE " + FIELD_GROUPS_KEY + " = " + cursor.getInt(0), null);
			
			if(!relayCursor.moveToFirst()) continue;
			
			do {
				rids.add(relayCursor.getInt(0));
			} while(relayCursor.moveToNext());
			
			groups.add(new RelayGroup(cursor.getInt(0), cursor.getString(1), rids, false));
		} while(cursor.moveToNext());
		
		db.close();
		return groups;
	}
	
	
	public void insertRelayGroup(RelayGroup group) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Get info from the relay
		ContentValues values = new ContentValues();
		values.put(FIELD_RELAYS_NAME, group.getName());
		
		// Insert the values into the db
		int gid = (int)db.insert(TABLE_GROUPS, null, values);
		
		// Update each relay in the group with the gid
		updateRelayGids(db, gid, group.getRids());

		db.close();
	}
	
	
	public int updateRelayGroup(RelayGroup group) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that the group has a valid gid
		if(group.getGid() < 0) return Constants.FAILURE;
		
		// Remove the old rids from the group
		ContentValues values = new ContentValues();
		values.put(FIELD_RELAYS_GROUP, -1);
		db.update(TABLE_RELAYS, values, FIELD_RELAYS_GROUP + " = " + group.getGid(), null);
		
		// Get info from the group
		values = new ContentValues();
		values.put(FIELD_GROUPS_NAME, group.getName());
		
		db.update(TABLE_GROUPS, values, FIELD_GROUPS_KEY + " = " + group.getGid(), null);

		// Update each relay with the new gid
		updateRelayGids(db, group.getGid(), group.getRids());

		db.close();
		return Constants.SUCCESS;
	}
	
	
	public int deleteRelayGroup(RelayGroup group) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that the group has a valid gid
		if(group.getGid() < 0) return Constants.FAILURE;
		
		db.delete(TABLE_GROUPS, FIELD_GROUPS_KEY + " = " + group.getGid(), null);
		
		// Remove the relays from the group
		updateRelayGids(db, -1, group.getRids());
		
		db.close();
		return Constants.SUCCESS;
	}
	
	
	private void updateRelayGids(SQLiteDatabase db, int gid, ArrayList<Integer> rids) {
		ContentValues values;
		
		for(int rid : rids) {
			values = new ContentValues();
			values.put(FIELD_RELAYS_GROUP, gid);
			
			db.update(TABLE_RELAYS, values, FIELD_RELAYS_KEY + " = " + rid, null);
		}
	}
	
	
	
	public Bundle selectWidget(int wid) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for the given widget with wid
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WIDGETS + " WHERE " + FIELD_WIDGETS_KEY + " = " + wid, null);
		
		// Go to the first result or result null if it doesn't exist
		if(!cursor.moveToFirst()) {
			db.close();
			return null;
		}
		
		// Put the contents in a bundle
		Bundle widgetInfo = new Bundle();
		widgetInfo.putInt(FIELD_WIDGETS_KEY, cursor.getInt(0));
		widgetInfo.putInt(FIELD_WIDGETS_TYPE, cursor.getInt(1));
		widgetInfo.putInt(FIELD_WIDGETS_ID, cursor.getInt(2));
		
		db.close();
		return widgetInfo;
	}
	
	
	public ArrayList<Bundle> selectAllWidgets() {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for all widgets
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WIDGETS, null);
		
		// Add each result to an ArrayList
		ArrayList<Bundle> widgetInfoBundles = new ArrayList<Bundle>();
		
		// If no results, just close the connection and return an empty list
		if(!cursor.moveToFirst()) {
			db.close();
			return widgetInfoBundles;
		}
		
		do{
			Bundle widgetInfo = new Bundle();
			widgetInfo.putInt(FIELD_WIDGETS_KEY, cursor.getInt(0));
			widgetInfo.putInt(FIELD_WIDGETS_TYPE, cursor.getInt(1));
			widgetInfo.putInt(FIELD_WIDGETS_ID, cursor.getInt(2));
			widgetInfoBundles.add(widgetInfo);
		} while(cursor.moveToNext());
		
		db.close();
		return widgetInfoBundles;
	}
	
	
	public int insertWidget(int wid, int type, int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that type and ID are valid
		if(wid < 0 || (type != Constants.WIDGET_RELAY && type != Constants.WIDGET_GROUP) || id < 0) {
			return Constants.FAILURE;
		}
		
		ContentValues values = new ContentValues();
		values.put(FIELD_WIDGETS_KEY, wid);
		values.put(FIELD_WIDGETS_TYPE, type);
		values.put(FIELD_WIDGETS_ID, id);
		
		// Insert the values into the db
		db.insert(TABLE_WIDGETS, null, values);
		db.close();
		
		return Constants.SUCCESS;
	}
	
	
	public int updateWidget(int wid, int type, int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that type and ID are valid
		if(wid < 0 || (type != Constants.WIDGET_RELAY && type != Constants.WIDGET_GROUP) || id < 0) {
			return Constants.FAILURE;
		}
		
		ContentValues values = new ContentValues();
		values.put(FIELD_WIDGETS_KEY, wid);
		values.put(FIELD_WIDGETS_TYPE, type);
		values.put(FIELD_WIDGETS_ID, id);
		
		db.update(TABLE_WIDGETS, values, FIELD_WIDGETS_KEY + " = " + wid, null);
		db.close();
		
		return Constants.SUCCESS;
	}
	
	
	public int deleteWidget(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that the id is valid
		if(id < 0) return Constants.FAILURE;
		
		db.delete(TABLE_WIDGETS, FIELD_WIDGETS_KEY + " = " + id, null);
		db.close();
		
		return Constants.SUCCESS;
	}
}
