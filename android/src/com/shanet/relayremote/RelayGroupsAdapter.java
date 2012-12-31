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

public class RelayGroupsAdapter extends ArrayAdapter<RelayGroup> {
    
    private Context context;
    private ArrayList<RelayGroup> groups;
    
    public RelayGroupsAdapter(Context context, ArrayList<RelayGroup> groups) {
        super(context, R.layout.relay_adapter, groups);
        this.context = context;
        this.groups = groups;
    }
    
    public View getView(final int position, View view, ViewGroup parent) {
        // Inflate the view if it has not  been created yet
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.relay_adapter, null);
        }
        
        TextView relayName        = (TextView) view.findViewById(R.id.relayName);
        final Switch groupSwitch = (Switch) view.findViewById(R.id.relaySwitch);
        
        // Set the name on the label
        relayName.setText(groups.get(position).getName() + ":");
        
        // Set the switch to on if the group state is on
        if(groups.get(position).isOn()) {
            groupSwitch.setChecked(true);
        } else {
            groupSwitch.setChecked(false);
        }
        
        // Use an on click listener so that the bg thread is only start if a user presses the button
        // rather than using an on change listener which can be triggered programmatically when the
        // relays states are checked
        groupSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RelayGroup group = groups.get(position);
                Database db = new Database(context);
                
                // Turn on/off each relay in the group
                ArrayList<Integer> rids = group.getRids();
                for(int rid : rids) {
                    Utils.startNetworkThreadForRelay(context, db.selectRelay(rid), (groupSwitch.isChecked()) ? Constants.CMD_ON : Constants.CMD_OFF);
                }
            }
        });
        
        return view;
    }
    
    public void updateGroups(ArrayList<RelayGroup> groups) {
        // Make a copy of the relays list so that the clear operation doesn't
        // clear it as well since may point to the same reference
        ArrayList<RelayGroup> groups_copy = new ArrayList<RelayGroup>();
        for(int i=0; i<groups.size(); i++) {
            groups_copy.add(groups.get(i));
        }
        
        // Clear the list
        clear();

        // Set the new data
        this.groups = groups_copy;
        addAll(this.groups);
        notifyDataSetChanged();
    }
}