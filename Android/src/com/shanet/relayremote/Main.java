package com.shanet.relayremote;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;

public class Main extends Activity {
	
	private ArrayList<Relay> relays;
	private ListView relayList;
	private RelayAdapter relayAdapter;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Show the welcome or changelog dialog if necessary
		Utils.showOpeningDialogs(this);
				
		// Setup the relay list
		setupRelayList();
		
		// Get the relay states
		updateRelayStates();
	}
	
	
	public void updateRelayStates() {		
		// For each unique server, start a thread to get the state of the relays on that server
		ArrayList<String> servers = new ArrayList<String>();
		Bundle bgInfo = new Bundle();
		Relay relay;
		for(int i=0; i<relays.size(); i++) {
			relay = relays.get(i);
			if(!servers.contains(relay.getServer())) {
				bgInfo.putChar("op", Constants.OP_GET);
				bgInfo.putString("server", relay.getServer());
				bgInfo.putInt("port", relay.getPort());
				
				// Add this server to the server list so we don't check it again
				servers.add(relay.getServer());
				
				new Background(this, Constants.OP_GET).execute(bgInfo);
			}
		}
	}
	
	
	public void setRelayStates(ArrayList<BasicNameValuePair> states) {
		// The server these states correspond to is the first entry
		String server = states.get(0).getValue();
		
		Relay relay;
		for(int i=0; i<relays.size(); i++) {
			relay = relays.get(i);
			
			// If the current relay belongs to the server the states belong to, find it's state by matching pins
			if(relay.getServer().equals(server)) {
				for(int j=1; j<states.size(); j++) {
					if(relay.getPin() == Integer.valueOf(states.get(j).getName())) {
						relay.setIsOn(states.get(j).getValue().charAt(0) == Constants.CMD_ON);
					}
				}
			}
		}
		
		// Update the relay adapter with the new relays
		relayAdapter.updateRelays(relays);
	}
	
	
	public void reloadRelays() {
		// Reload the relays from the db and update the list in the list adapter
		relays = new Database(this).selectAllRelays();
		relayAdapter.updateRelays(relays);
	}
	
	
	private void setupRelayList() {
		relayList = (ListView) findViewById(R.id.relayList);
		
		// Set the empty list layout on the relay list
		relayList.setEmptyView(findViewById(R.id.emptyListLayout));
		
		// Load all relays from the db and set the list adapter
		relays = new Database(this).selectAllRelays();
		relayAdapter = new RelayAdapter(this, relays);
		relayList.setAdapter(relayAdapter);
		
		// Set the long click menu on the relay list
		registerForContextMenu(relayList);
		
		// Add a click listener to the empty list add relay button
		((Button)findViewById(R.id.emptyAddRelay)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Main.this.startActivityForResult(new Intent(Main.this, AddRelay.class), Constants.ADD_EDIT_CODE);
			}
		});
	}
	
	
	public void turnOnOffAllRelays(char cmd) {
		for(int i=0; i<relays.size(); i++) {
			Relay relay = relays.get(i);
			
			// Create the background info bundle
			Bundle bgInfo = new Bundle();
			bgInfo.putChar("op", Constants.OP_SET);
			bgInfo.putString("server", relay.getServer());
			bgInfo.putInt("port", relay.getPort());
			bgInfo.putInt("pin", relay.getPin());
			bgInfo.putChar("cmd", cmd);
			
			// Call the bg thread to send the commands to the server
			new Background(this, Constants.OP_SET).execute(bgInfo);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	getMenuInflater().inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Get the selected relay
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		Relay selectedRelay = relays.get(menuInfo.position);
		
 		switch (item.getItemId()) {
			case R.id.deleteRelay:
				DialogUtils.showDeleteDialog(this, selectedRelay);
				return true;
			case R.id.editRelay:
				// Pass the relay id (rid) of the selected relay to the edit activity
				Intent intent = new Intent(this, EditRelay.class);
				intent.putExtra("rid", selectedRelay.getRid());
				
				// Start the add/edit activity
				startActivityForResult(intent, Constants.ADD_EDIT_CODE);
				return true;
			default:
				return false;
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// If the activity is the edit relay activity, we should update the relays
		if(requestCode == Constants.ADD_EDIT_CODE) {
			reloadRelays();
		}
	}

	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_options_menu, menu);
		return true;
	}
	

	public boolean onOptionsItemSelected(MenuItem item) {
		return Utils.onOptionsItemSelected(this, item);
	}
}
