package com.shanet.relayremote;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class EditRelay extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.add_edit_relay);
        
        // Get the relay to edit
        Database db = new Database(this);
        final Relay editRelay = db.selectRelay(getIntent().getIntExtra("rid", -1));
                
        final Spinner pinSpinner  = (Spinner) findViewById(R.id.pinSpinner);
        final EditText nameText   = (EditText) findViewById(R.id.addEditName);
        final EditText serverText = (EditText) findViewById(R.id.addEditServer);
        final EditText portText   = (EditText) findViewById(R.id.addEditPort);
        Button editRelayButton     = (Button) findViewById(R.id.addEditRelay);
        
        // Change the edit relay button text since the layout is shared with the add relay activity
        editRelayButton.setText(R.string.updateRelay);
        
        // Get the position of the relay's current pin in the entries array
        int position = 0;
        String[] entries = getResources().getStringArray(R.array.pinSpinnerEntries);
        for(int i=0; i<entries.length; i++) {
        	if(Integer.valueOf(entries[i].substring(entries[i].length()-1)) == editRelay.getPin()) {
        		position = i;
        		break;
        	}
        }
        
        // Set the default pin
        pinSpinner.setSelection(position);
        
        // Set the name in the UI
        nameText.setText(editRelay.getName());
        
        // Set the server in the UI
        serverText.setText(editRelay.getServer());
        
        // Set the port in the UI
        portText.setText(Integer.valueOf(editRelay.getPort()).toString());
        
        editRelayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get the selected pin
				String selectedPin = (String) pinSpinner.getSelectedItem();
				int pin = Integer.valueOf(selectedPin.substring(selectedPin.length()-1));
				
				// Check that the port isn't empty
				if(portText.getText().toString().equals("")) {
					DialogUtils.displayErrorDialog(EditRelay.this, R.string.emptyPortErrorTitle, R.string.emptyPortError);
					return;
				}
				
				// Check that the server isn't empty
				String server = serverText.getText().toString();
				if(server.equals("")) {
					DialogUtils.displayErrorDialog(EditRelay.this, R.string.emptyServerErrorTitle, R.string.emptyServerError);
					return;
				}
				
				// Check that the name isn't empty
				String name = nameText.getText().toString();
				if(name.equals("")) {
					DialogUtils.displayErrorDialog(EditRelay.this, R.string.emptyNameErrorTitle, R.string.emptyNameError);
				}
				
				// Check the port range
				int port = Integer.valueOf(portText.getText().toString());
				if(port < 1 || port > 65535) {
					DialogUtils.displayErrorDialog(EditRelay.this, R.string.portRangeErrorTitle, R.string.portRangeError);
					return;
				}
				
				// Update the fields in the relay
				editRelay.setPin(pin);
				editRelay.setName(name);
				editRelay.setServer(server);
				editRelay.setPort(port);
				
				// Update the relay in the db
				Database db = new Database(EditRelay.this);
				db.updateRelay(editRelay);
				
				Toast.makeText(EditRelay.this, R.string.editedRelay, Toast.LENGTH_SHORT).show();
				
				// Return to the calling activity
				finish();
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
