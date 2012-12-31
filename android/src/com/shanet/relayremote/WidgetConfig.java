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

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WidgetConfig extends ListActivity {
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        
        final int appWidgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        
        ListView relayList = getListView();
        
        // Load all relays from the db
        Database db = new Database(this);
        final ArrayList<Relay> relays = db.selectAllRelays();
        final ArrayList<RelayGroup> groups = db.selectAllRelayGroups();
        
        // Create a list of the names of all relays and groups for the list adapter
        ArrayList<String> names = new ArrayList<String>();
        for(Relay relay : relays) {
            names.add(relay.getName());
        }
        for(RelayGroup group : groups) {
            names.add(group.getName());
        }
        
        relayList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, names));
        
        // Create a textview if no relays/groups exist yet
        TextView emptyText = new TextView(this);
        emptyText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        emptyText.setText(R.string.emptyText);
        relayList.setEmptyView(emptyText);
                
        relayList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If the position is in the first [relays.size()-1] elements, it is a relay, not a group
                int type = (position < relays.size()) ? Constants.WIDGET_RELAY : Constants.WIDGET_GROUP;
                
                // Get the rid or gid of the selected relay/group
                int _id;
                if(type == Constants.WIDGET_RELAY) {
                    _id = relays.get(position).getRid(); 
                } else {
                    _id = groups.get(position-relays.size()).getGid();
                }
                
                // Prepare the result value intent and call onUpdate() on the new widget if successfully added
                Intent intent = new Intent(WidgetConfig.this, Widget.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {appWidgetId});
                
                // Insert the widget in the db
                if(new Database(WidgetConfig.this).insertWidget(appWidgetId, type, _id) == Constants.SUCCESS) {
                    setResult(RESULT_OK, intent);
                    sendBroadcast(intent);
                } else {
                    setResult(RESULT_CANCELED, intent);
                }
                
                finish();
            }
        });
    }
}
