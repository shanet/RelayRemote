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

public class AddRelayGroup extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.add_edit_relay_group);

    final EditText nameText   = (EditText) findViewById(R.id.addEditGroupName);
    final ListView relayList  = (ListView) findViewById(R.id.addEditRelayList);
    Button addGroupButton     = (Button) findViewById(R.id.addEditRelayGroup);
    Button emptyButton        = (Button) findViewById(R.id.emptyAddRelayGroup);

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

    // If the list is empty, remove the other views
    if(relayList.getAdapter().getCount() == 0) {
      findViewById(R.id.addEditGroupNameLabel).setVisibility(View.GONE);
      findViewById(R.id.addEditRelayListLabel).setVisibility(View.GONE);
      nameText.setVisibility(View.GONE);
      addGroupButton.setVisibility(View.GONE);
    }

    // Set the click listener on the add group button
    addGroupButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // Check that the name isn't empty
        String name = nameText.getText().toString();
        if(name.equals("")) {
          DialogUtils.displayErrorDialog(AddRelayGroup.this, R.string.emptyNameErrorTitle, R.string.emptyNameError);
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
          DialogUtils.displayErrorDialog(AddRelayGroup.this, R.string.noGroupsSelectedErrorTitle, R.string.noGroupsSelectedError);
          return;
        }


        // Add the new group to the db
        Database db = new Database(AddRelayGroup.this);
        db.insertRelayGroup(new RelayGroup(name, rids));

        Toast.makeText(AddRelayGroup.this, R.string.createdGroup, Toast.LENGTH_SHORT).show();

        // Return to the calling activity
        finish();
      }
    });

    // Set the click listener on the empty add relay button
    emptyButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        AddRelayGroup.this.startActivity(new Intent(AddRelayGroup.this, AddRelay.class));
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
