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

import android.content.Context;
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
        } else {
            relaySwitch.setChecked(false);
        }
        
        // Use an on click listener so that the bg thread is only start if a user presses the button
        // rather than using an on change listener which can be triggered programmatically when the
        // relays states are checked
        relaySwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startNetworkThreadForRelay(context, relays.get(position), (relaySwitch.isChecked()) ? Constants.CMD_ON : Constants.CMD_OFF);
                
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