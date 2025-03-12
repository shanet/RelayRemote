package com.shanet.relayremote;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class EditRelayGroup extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.add_edit_relay_group);

    // Get the group to edit
    final Database db = new Database(this);
    final RelayGroup editGroup = db.selectRelayGroup(getIntent().getIntExtra("gid", -1));

    final EditText nameText   = (EditText) findViewById(R.id.addEditGroupName);
    final ListView relayList  = (ListView) findViewById(R.id.addEditRelayList);
    Button editGroupButton     = (Button) findViewById(R.id.addEditRelayGroup);
    Button emptyButton         = (Button) findViewById(R.id.emptyAddRelayGroup);

    // Change the edit relay button text since the layout is shared with the add relay activity
    editGroupButton.setText(R.string.updateGroup);

    // Fill the list with all available relays
    final ArrayList<Relay> relays = new Database(this).selectAllRelays();
    ArrayList<String> relayNames = new ArrayList<String>();
    for(Relay relay : relays) {
      relayNames.add(relay.getName());
    }
    relayList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    relayList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, relayNames));

    // Add the empty view to the list
    relayList.setEmptyView(findViewById(R.id.addGroupEmpty));

    // Set the relays already in the group as selected in the relay list
    for(int i=0; i<relays.size(); i++) {
      if(editGroup.getRids().contains(relays.get(i).getRid())) {
        relayList.setItemChecked(i, true);
      }
    }

    // Set the name in the UI
    nameText.setText(editGroup.getName());

    // If the list is empty, remove the other views
    if(relayList.getAdapter().getCount() == 0) {
      findViewById(R.id.addEditGroupNameLabel).setVisibility(View.GONE);
      findViewById(R.id.addEditRelayListLabel).setVisibility(View.GONE);
      nameText.setVisibility(View.GONE);
      editGroupButton.setVisibility(View.GONE);
    }

    // Set the click listener on the add group button
    editGroupButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // Check that the name isn't empty
        String name = nameText.getText().toString();
        if(name.equals("")) {
          DialogUtils.displayErrorDialog(EditRelayGroup.this, R.string.emptyNameErrorTitle, R.string.emptyNameError);
          return;
        }

        // Get the selected groups
        ArrayList<Integer> rids = new ArrayList<Integer>();

        // Get the selected indices
        SparseBooleanArray checkedItems = relayList.getCheckedItemPositions();

        // Add the selected items to the rids array
        for(int i=0; i<relays.size(); i++) {
          if(checkedItems.get(i)) {
            rids.add(relays.get(i).getRid());
          }
        }

        // Check if no relays were selected
        if(rids.size() == 0) {
          DialogUtils.displayErrorDialog(EditRelayGroup.this, R.string.noGroupsSelectedErrorTitle, R.string.noGroupsSelectedError);
          return;
        }

        // Update the fields in the group
        editGroup.setName(name);
        editGroup.setRids(rids);

        // Update the relay in the db
        db.updateRelayGroup(editGroup);

        Toast.makeText(EditRelayGroup.this, R.string.editedGroup, Toast.LENGTH_SHORT).show();

        // Return to the calling activity
        finish();
      }
    });

    // Set the click listener on the empty add relay button
    emptyButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        EditRelayGroup.this.startActivity(new Intent(EditRelayGroup.this, AddRelay.class));
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.generic_options_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return Utils.onOptionsItemSelected(this, item);
  }
}
