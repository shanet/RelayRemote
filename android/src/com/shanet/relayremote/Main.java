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
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Main extends FragmentActivity {
	
	private static final int RELAYS_FRAGMENT_NUM = 0;
	private static final int GROUPS_FRAGMENT_NUM = 1;
    
    private ArrayList<Relay> relays;
    private ArrayList<RelayGroup> relayGroups;
    private Database database;
    private int curFragment;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        // Open the database and load the relays and groups
        database = new Database(this);
        reloadRelaysAndGroupsFromDatabase();
        
        // Show the welcome or changelog dialog if necessary
        Utils.showOpeningDialogs(this);
        
        curFragment = 0;
        getRelayStates();
    }
    
    
    public class RelayPagerAdapter extends FragmentStatePagerAdapter {

        public RelayPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
        	System.out.println("getItem(" + position + ")");
            switch(position) {
                case RELAYS_FRAGMENT_NUM:
                    return new RelaysFragment();
                case GROUPS_FRAGMENT_NUM:
                    return new RelayGroupsFragment();
                default:
                    return null;
            }
        }

        public int getCount() {
            return 2;
        }

        public CharSequence getPageTitle(int position) {
            switch(position) {
                case RELAYS_FRAGMENT_NUM:
                    return getString(R.string.relays);
                case GROUPS_FRAGMENT_NUM:
                    return getString(R.string.groups);
            }
            return null;
        }
    }
    
    
    public ArrayList<Relay> getRelays() {
    	return relays;
    }
    
    
    public ArrayList<RelayGroup> getRelayGroups() {
    	return relayGroups;
    }
    
    
    public void reloadRelaysAndGroupsFromDatabase() {
    	relays      = database.selectAllRelays();
    	relayGroups = database.selectAllRelayGroups();
    }
    
    
    private void updatePagerAdapter() {
        // Create the adapter that will return the relay and relay groups fragments
        RelayPagerAdapter pagerAdapter = new RelayPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        
        // Set the pager to the current fragment
        pager.setCurrentItem(curFragment);
        
        // Listen for pager changes to keep track of the currently displayed fragment so the state
        // can restored when this function is called
        pager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                curFragment = position;
            }
        });
    }
 
    
    public void getRelayStates() {
        Toast.makeText(this, R.string.refreshingRelays, Toast.LENGTH_SHORT).show();

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
                
                new Background(this, Constants.OP_GET, false).execute(bgInfo);
            }
        }
    }
    
    
    public void setRelaysAndGroupsStates(ArrayList<BasicNameValuePair> states) {
    	setRelayStates(states);
    	setGroupStates();
    	
        // Update the relay and group fragments adapters with the new states
        updatePagerAdapter();
    }
    
    
    private void setRelayStates(ArrayList<BasicNameValuePair> states) {
        // The server these states correspond to is the first entry
        String server = states.get(0).getValue();
        
        Relay relay;
        ArrayList<Bundle> widgets = database.selectAllWidgets();

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
                                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
                                views.setImageViewResource(R.id.widgetIndicator, (states.get(i).getValue().charAt(0) == Constants.CMD_ON) ? R.drawable.widget_on : R.drawable.widget_off);
                                AppWidgetManager.getInstance(this).updateAppWidget(widget.getInt("wid"), views);
                                
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
    }
    
    
    private void setGroupStates() {
        // If no groups exist, don't do anything
        if(relayGroups.size() == 0) return;
        
        RelayGroup group;

        // Check each group to see if all relays in it are on; if not, the group is considered off
        for(int i=0; i<relayGroups.size(); i++) {
            group = relayGroups.get(i);
            
            boolean isGroupOn = true;
            rid_loop:
            for(int rid : group.getRids()) {
                for(Relay relay : relays) {
                    // Check if this relay is in the group
                    if(rid == relay.getRid()) {
                        // If the relay is in the group but it's off, then the group is considered off
                        if(!relay.isOn()) {
                            isGroupOn = false;
                            break rid_loop;
                        }
                        
                        // Done here. Check the next rid
                        continue rid_loop;
                    }
                }
            }

            // If the group on boolean is still true, all rids in the group are on
            if(isGroupOn) {
                group.turnOn();
            } else {
                group.turnOff();
            }
        }
    }
    
    
    public void turnOnOffAllRelays(char cmd) {
        for(Relay relay : relays) {
            Utils.startNetworkThreadForRelay(this, relay, cmd);
        }
    }
    
    
    public void turnOnOffAllGroups(char cmd) {
        for(int i=0; i<relayGroups.size(); i++) {            
            RelayGroup group = relayGroups.get(i);
            
            // Turn on/off each relay in the group
            ArrayList<Integer> rids = group.getRids();
            for(int rid : rids) {
                // Load the relay from the db
                Relay relay = database.selectRelay(rid);
                
                Utils.startNetworkThreadForRelay(this, relay, cmd);    
            }
        }
    }
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // If the activity is the edit relay/group activity, we should update the relays
        if(requestCode == Constants.ADD_EDIT_CODE) {
        	reloadRelaysAndGroupsFromDatabase();
        }
    }
    
    
    public void onNewIntent(Intent intent) {
        // When an NFC tag is being written, call the write tag function when an intent is
        // received that says the tag is within range of the device and ready to be written to
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String nfcMessage = intent.getStringExtra("nfcMessage");

        if(nfcMessage != null) {
            NFC.writeTag(this, tag, nfcMessage);
        }
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
