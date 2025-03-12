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

public class RelaysFragment extends ListFragment {
  private View layout;
  private ListView relayList;
  private RelayAdapter relayAdapter;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    layout = inflater.inflate(R.layout.relays_fragment, null);

    // Setup the relay list
    setupRelayList();

    // Tell Android this fragment has an options menu
    setHasOptionsMenu(true);

    return layout;
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);

    // If visible, reload the relays
    if(this.isVisible() && isVisibleToUser) {
      reloadRelays();
    }
  }

  public void reloadRelays() {
    ArrayList<Relay> relays = ((Main)getActivity()).getRelays();
    relayAdapter.updateRelays(relays);
  }

  private void setupRelayList() {
    relayList = (ListView) layout.findViewById(android.R.id.list);

    // Load all relays from the db and set the list adapter
    ArrayList<Relay> relays = ((Main)getActivity()).getRelays();
    if(relays == null) System.out.println("setup relays is null");

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

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    getActivity().getMenuInflater().inflate(R.menu.relays_context_menu, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    if(!getUserVisibleHint()) return false;

    // Get the selected relay
    AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    Relay selectedRelay = ((Main)getActivity()).getRelays().get(menuInfo.position);

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
