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

public class RelayGroupsFragment extends ListFragment {

    private View layout;
    private ListView groupsList;
    private RelayGroupsAdapter groupsAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.relays_group_fragment, null);

        // Setup the groups list
        setupRelayGroupsList();

        // Tell Android this fragment has an options menu
        setHasOptionsMenu(true);

        return layout;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // If visible, reload the relays
        if(this.isVisible() && isVisibleToUser) {
            reloadGroups();
        }
    }


    public void reloadGroups() {
    	ArrayList<RelayGroup> groups = ((Main)getActivity()).getRelayGroups();
        groupsAdapter.updateGroups(groups);
    }


    private void setupRelayGroupsList() {
        groupsList = (ListView) layout.findViewById(android.R.id.list);

        // Load all relays from the db and set the list adapter
    	ArrayList<RelayGroup> groups = ((Main)getActivity()).getRelayGroups();
        groupsAdapter = new RelayGroupsAdapter(getActivity(), groups);
        groupsList.setAdapter(groupsAdapter);

        // Set the long click menu on the relay list
        registerForContextMenu(groupsList);

        // Add a click listener to the empty list add relay button
        ((Button)layout.findViewById(R.id.emptyAddRelay)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivityForResult(new Intent(getActivity(), AddRelayGroup.class), Constants.ADD_EDIT_CODE);
            }
        });
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.groups_context_menu, menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(!getUserVisibleHint()) return false;

        // Get the selected relay
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        RelayGroup selectedGroup = ((Main)getActivity()).getRelayGroups().get(menuInfo.position);

        switch (item.getItemId()) {
            case R.id.deleteGroup:
                DialogUtils.showDeleteDialog(getActivity(), selectedGroup);
                return true;

            case R.id.editGroup:
                // Pass the group id (gid) of the selected group to the edit activity
                Intent intent = new Intent(getActivity(), EditRelayGroup.class);
                intent.putExtra("gid", selectedGroup.getGid());

                // Start the add/edit activity
                getActivity().startActivityForResult(intent, Constants.ADD_EDIT_CODE);
                return true;

            case R.id.createNFCGroup:
                if(Utils.hasNfcSupport(getActivity())) {
                    DialogUtils.displayNfcTypeDialog(getActivity(), Constants.NFC_GROUP, selectedGroup.getGid());
                }

                return true;

            default:
                return false;
        }
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.relay_groups_frag_options_menu, menu);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        return Utils.onOptionsItemSelected(getActivity(), item);
    }
}
