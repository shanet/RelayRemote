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

public class AddRelay extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_edit_relay);

        final Spinner pinSpinner  = (Spinner) findViewById(R.id.pinSpinner);
        final EditText nameText   = (EditText) findViewById(R.id.addEditName);
        final EditText serverText = (EditText) findViewById(R.id.addEditServer);
        final EditText portText   = (EditText) findViewById(R.id.addEditPort);
        Button addRelayButton      = (Button) findViewById(R.id.addEditRelay);

        // Get the position of the default pin in the entries array
        int position = 0;
        String[] entries = getResources().getStringArray(R.array.pinSpinnerEntries);
        for(int i=0; i<entries.length; i++) {
            if(Integer.valueOf(entries[i].substring(entries[i].length()-1)) == Constants.DEFAULT_PIN) {
                position = i;
                break;
            }
        }

        // Set the default pin
        pinSpinner.setSelection(position);

        // Set the default port
        portText.setText(Integer.valueOf(Constants.DEFAULT_PORT).toString());

        addRelayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected pin
                String selectedPin = (String) pinSpinner.getSelectedItem();
                int pin = Integer.valueOf(selectedPin.substring(selectedPin.length()-1));

                // Check that the port isn't empty
                if(portText.getText().toString().equals("")) {
                    DialogUtils.displayErrorDialog(AddRelay.this, R.string.emptyPortErrorTitle, R.string.emptyPortError);
                    return;
                }

                // Check that the server isn't empty
                String server = serverText.getText().toString();
                if(server.equals("")) {
                    DialogUtils.displayErrorDialog(AddRelay.this, R.string.emptyServerErrorTitle, R.string.emptyServerError);
                    return;
                }

                // Check that the name isn't empty
                String name = nameText.getText().toString();
                if(name.equals("")) {
                    DialogUtils.displayErrorDialog(AddRelay.this, R.string.emptyNameErrorTitle, R.string.emptyNameError);
                    return;
                }

                // Check the port range
                int port = Integer.valueOf(portText.getText().toString());
                if(port < 1 || port > 65535) {
                    DialogUtils.displayErrorDialog(AddRelay.this, R.string.portRangeErrorTitle, R.string.portRangeError);
                    return;
                }

                // Add the new relay to the db
                Database db = new Database(AddRelay.this);
                db.insertRelay(new Relay(name, pin, server, port));

                Toast.makeText(AddRelay.this, R.string.addedRelay, Toast.LENGTH_SHORT).show();

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
