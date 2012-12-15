package com.shanet.relayremote;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

public class RelayAdapter extends ArrayAdapter<Relay> {
	private Context context;
	private ArrayList<Relay> relays;
	
	public RelayAdapter(Context context, ArrayList<Relay> relays) {
		super(context, R.layout.relay_adapter, relays);
		this.context = context;
		this.relays = relays;
	}
	
	public View getView(final int position, View view, ViewGroup parent) {
		// Inflate the view if it has not  been created yet
		if(view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.relay_adapter, null);
		}
		
		TextView relayName       = (TextView) view.findViewById(R.id.relayName);
		final Switch relaySwitch = (Switch) view.findViewById(R.id.relaySwitch);
		
		// Set the name on the label
		relayName.setText(relays.get(position).getName() + ":");
		
		// Set the switch to on if the relay state is on
		if(relays.get(position).isOn()) {
			relaySwitch.setChecked(true);
		}
		
		// Use an on click listener so that the bg thread is only start if a user presses the button
		// rather than using an on change listener which can be triggered programmatically when the
		// relays states are checked
		relaySwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Relay relay = relays.get(position);
				
				// Create the background info bundle
				Bundle bgInfo = new Bundle();
				bgInfo.putChar("op", Constants.OP_SET);
				bgInfo.putString("server", relay.getServer());
				bgInfo.putInt("port", relay.getPort());
				bgInfo.putInt("pin", relay.getPin());
				bgInfo.putChar("cmd", (relaySwitch.isChecked()) ? Constants.CMD_ON : Constants.CMD_OFF);
				
				// Call the bg thread to send the commands to the server
				new Background(context, Constants.OP_SET).execute(bgInfo);
				
			}
		});
		
		return view;
	}
	
	public void updateRelays(ArrayList<Relay> relays) {
		// Make a copy of the relays list so that the clear operation doesn't
		// clear it as well since may point to the same reference
		ArrayList<Relay> relays_copy = new ArrayList<Relay>();
		for(int i=0; i<relays.size(); i++) {
			relays_copy.add(relays.get(i));
		}
		
		// Clear the list
		clear();

		// Set the new data
		this.relays = relays_copy;
		addAll(this.relays);
		notifyDataSetChanged();
	}
}