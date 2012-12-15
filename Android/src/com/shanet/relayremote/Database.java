package com.shanet.relayremote;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "RelayRemote";
	private static final int DATABASE_VERSION = 2;
	
	private static final String TABLE_RELAYS = "relays";
	private static final String FIELD_KEY    = "rid";
	private static final String FIELD_NAME   = "name";
	private static final String FIELD_PIN    = "pin";
	private static final String FIELD_SERVER = "server";
	private static final String FIELD_PORT   = "port";
	
	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void onCreate(SQLiteDatabase db) {
		// Create the relays table
		db.execSQL("CREATE TABLE " + TABLE_RELAYS + "(" + 
	               FIELD_KEY    + " INTEGER PRIMARY KEY, " +
				   FIELD_NAME   + " TEXT, " +
				   FIELD_PIN    + " INTEGER, " +
	               FIELD_SERVER + " TEXT, " +
				   FIELD_PORT   + " INTEGER)");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop the old table
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RELAYS);
		
		// Create a new table
		onCreate(db);
	}
	
	public Relay selectRelay(int rid) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for the given relay with rid
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RELAYS + " WHERE rid = " + rid, null);
		
		// Go to the first result or result null if it doesn't exist
		if(!cursor.moveToFirst()) {
			db.close();
			return null;
		}
		
		// Create a new relay object and return it
		Relay relay = new Relay(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getInt(4), false);
		
		db.close();
		return relay;
	}
	
	public ArrayList<Relay> selectAllRelays() {
		SQLiteDatabase db = this.getReadableDatabase();
		
		// Query the db for the given relay with rid
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RELAYS, null);
		
		// Add each result to an ArrayList
		ArrayList<Relay> relays = new ArrayList<Relay>();
		
		// If no results, just close the connection and return an empty list
		if(!cursor.moveToFirst()) {
			db.close();
			return relays;
		}
		
		do{
			relays.add(new Relay(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getInt(4), false));
		} while(cursor.moveToNext());
		
		db.close();
		return relays;
	}
	
	public void insertRelay(Relay relay) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Get info from the relay
		ContentValues values = new ContentValues();
		values.put(FIELD_NAME, relay.getName());
		values.put(FIELD_PIN, relay.getPin());
		values.put(FIELD_SERVER, relay.getServer());
		values.put(FIELD_PORT, relay.getPort());
		
		// Insert the values into the db
		db.insert(TABLE_RELAYS, null, values);
		db.close();
	}
	
	public int updateRelay(Relay relay) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that the relay has a valid rid
		if(relay.getRid() < 0) return Constants.FAILURE;
		
		// Get info from the relay
		ContentValues values = new ContentValues();
		values.put(FIELD_NAME, relay.getName());
		values.put(FIELD_PIN, relay.getPin());
		values.put(FIELD_SERVER, relay.getServer());
		values.put(FIELD_PORT, relay.getPort());
		
		db.update(TABLE_RELAYS, values, FIELD_KEY + "=" + relay.getRid(), null);
		db.close();
		
		return Constants.SUCCESS;
	}
	
	public int deleteRelay(Relay relay) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check that the relay has a valid rid
		if(relay.getRid() < 0) return Constants.FAILURE;
		
		db.delete(TABLE_RELAYS, FIELD_KEY + "=" + relay.getRid(), null);
		db.close();
		
		return Constants.SUCCESS;
	}
}
