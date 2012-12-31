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

import org.apache.http.message.BasicNameValuePair;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews;

public class RelaysFragment extends ListFragment {
    
    private ArrayList<Relay> relays;
    private ListView relayList;
    private RelayAdapter relayAdapter;
    private View layout;
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.relays_fragment, null);
        
        // Setup the relay list
        setupRelayList();
        
        // Get the relay states
        updateRelayStates();
        
        // Tell Android this fragment has an options menu
        setHasOptionsMenu(true);
        
        return layout;
    }
    
    
    public void setRelayStates(ArrayList<BasicNameValuePair> states) {
        // The server these states correspond to is the first entry
        String server = states.get(0).getValue();
        
        if(relays == null) {
            reloadRelays();
        }
        
        Relay relay;
        ArrayList<Bundle> widgets = new Database(getActivity()).selectAllWidgets();

        relay_loop:
        for(int i=0; i<relays.size(); i++) {
            relay = relays.get(i);
            
            // If the current relay belongs to the server the states belong to, find it's state by matching pins
            if(relay.getServer().equals(server)) {
                for(int j=1; j<states.size(); j++) {
                    if(relay.getPin() == Integer.valueOf(states.get(j).getName())) {
                        if(states.get(j).getValue().charAt(0) == Constants.CMD_ON) {
                            relay.turnOn();
                        } else {
                            relay.turnOff();
                        }
                        
                        // Check if any widgets are assigned to this relay, and if so, update them
                        for(Bundle widget : widgets) {
                            if(widget.getInt("type") == Constants.WIDGET_RELAY && widget.getInt("id") == relay.getRid()) {
                                // Update the indicator image
                                RemoteViews views = new RemoteViews(getActivity().getPackageName(), R.layout.widget);
                                views.setImageViewResource(R.id.widgetIndicator, (states.get(i).getValue().charAt(0) == Constants.CMD_ON) ? R.drawable.widget_on : R.drawable.widget_off);
                                AppWidgetManager.getInstance(getActivity()).updateAppWidget(widget.getInt("wid"), views);
                                
                                // Set the state of the widget in the widget class
                                Widget.setState(widget.getInt("wid"), (states.get(i).getValue().charAt(0) == Constants.CMD_ON) ? Widget.STATE_ON : Widget.STATE_OFF);
                            }
                        }
                        
                        // Done with this relay; move to the next one
                        continue relay_loop;
                    }
                }
            }
        }
        
        // Update the relay adapter with the new relays
        relayAdapter.updateRelays(relays);
        
        // Update the group states
        ((Main)getActivity()).getRelayGroupsFrag().updateGroupStates();
    }
    
    
    public void reloadRelays() {
        // Reload the relays from the db and update the list in the list adapter
        relays = new Database(getActivity()).selectAllRelays();
        relayAdapter.updateRelays(relays);
    }
    
    
    private void setupRelayList() {
        relayList = (ListView) layout.findViewById(android.R.id.list);
        
        // Load all relays from the db and set the list adapter
        relays = new Database(getActivity()).selectAllRelays();
        relayAdapter = new RelayAdapter(getActivity(), relays);
        relayList.setAdapter(relayAdapter);
        
        // Set the long click menu on the relay list
        registerForContextMenu(relayList);
        
        // Add a click listener to the empty list add relay button
        ((Button)layout.findViewById(R.id.emptyAddRelay)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivityForResult(new Intent(getActivity(), AddRelay.class), Constants.ADD_EDIT_CODE);
            }
        });
    }
    
    
    public void turnOnOffAllRelays(char cmd) {
        for(Relay relay : relays) {
            Utils.startNetworkThreadForRelay(getActivity(), relay, cmd);
        }
    }
    
    
    public void updateRelayStates() {
        // If no relays exist, don't do anything
        if(relays.size() == 0) return;
        
        // For each unique server, start a thread to get the state of the relays on that server
        ArrayList<String> servers = new ArrayList<String>();
        Relay relay;
        for(int i=0; i<relays.size(); i++) {
            relay = relays.get(i);
            if(!servers.contains(relay.getServer())) {
                Bundle bgInfo = new Bundle();
                bgInfo.putChar("op", Constants.OP_GET);
                bgInfo.putString("server", relay.getServer());
                bgInfo.putInt("port", relay.getPort());
                
                // Add this server to the server list so we don't check it again
                servers.add(relay.getServer());
                                
                new Background(getActivity(), Constants.OP_GET, false).execute(bgInfo);
            }
        }
    }
    
    
    public ArrayList<Relay> getRelays() {
        return relays;
    }
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.relays_context_menu, menu);
    }

    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Get the selected relay
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        Relay selectedRelay = relays.get(menuInfo.position);
        
        switch (item.getItemId()) {
            case R.id.deleteRelay:
                DialogUtils.showDeleteDialog(getActivity(), selectedRelay);
                return true;
                
            case R.id.editRelay:
                // Pass the relay id (rid) of the selected relay to the edit activity
                Intent intent = new Intent(getActivity(), EditRelay.class);
                intent.putExtra("rid", selectedRelay.getRid());
                
                // Start the add/edit activity
                getActivity().startActivityForResult(intent, Constants.ADD_EDIT_CODE);
                return true;
                
            case R.id.createNFC:
                if(Utils.hasNfcSupport(getActivity())) {
                    DialogUtils.displayNfcTypeDialog(getActivity(), Constants.NFC_RELAY, selectedRelay.getRid());
                }

                return true;
                
            default:
                return false;
        }
    }
    
    
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.relay_frag_options_menu, menu);
    }
    

    public boolean onOptionsItemSelected(MenuItem item) {
        return Utils.onOptionsItemSelected(getActivity(), item);
    }
}
