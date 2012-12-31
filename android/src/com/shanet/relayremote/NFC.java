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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

public class NFC extends Activity {
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        Intent intent = getIntent();
        if(intent.getType() != null && intent.getType().equals("application/" + getPackageName())) {
            // Read the first record which contains the relay info
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefRecord relayRecord = ((NdefMessage)rawMsgs[0]).getRecords()[0];
            String nfcData = new String(relayRecord.getPayload());
            
            // Decode the data on the nfc tag
            int type = nfcData.charAt(0) - 48;
            int id = Integer.valueOf(nfcData.substring(2, nfcData.lastIndexOf("-")));
            char cmd = nfcData.substring(nfcData.lastIndexOf("-")+1).charAt(0);
            
            Database db = new Database(this);
            
            // Based on the type (relay vs relay group) send the appropriate command to the server(s)
            if(type == Constants.NFC_RELAY) {
                startNetworkThread(db.selectRelay(id), cmd);
                
                Toast.makeText(this, R.string.nfcToast, Toast.LENGTH_SHORT).show();
            } else if(type == Constants.NFC_GROUP) {
                RelayGroup group = db.selectRelayGroup(id);
                
                // Turn on/off each relay in the group
                ArrayList<Integer> rids = group.getRids();
                for(int rid : rids) {
                    startNetworkThread(db.selectRelay(rid), cmd);
                }
                
                Toast.makeText(this, R.string.nfcToast, Toast.LENGTH_SHORT).show();
            }
            
            finish();
        }
    }
    
    
    private void startNetworkThread(Relay relay, char cmd) {
        // Create the background info bundle
        Bundle bgInfo = new Bundle();
        bgInfo.putChar("op", Constants.OP_SET);
        bgInfo.putString("server", relay.getServer());
        bgInfo.putInt("port", relay.getPort());
        bgInfo.putInt("pin", relay.getPin());
        bgInfo.putChar("cmd", cmd);
        
        // Call the bg thread to send the commands to the server
        new Background(this, Constants.OP_SET, true).execute(bgInfo);
    }
    
    
    public static boolean writeTag(Context context, Tag tag, String data) {     
        // Record to launch Play Store if app is not installed
        NdefRecord appRecord = NdefRecord.createApplicationRecord(context.getPackageName());
        
        // Record with actual data we care about
        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                                                new String("application/" + context.getPackageName()).getBytes(Charset.forName("US-ASCII")),
                                                null, data.getBytes());
        
        // Complete NDEF message with both records
        NdefMessage message = new NdefMessage(new NdefRecord[] {relayRecord, appRecord});

        try {
            // If the tag is already formatted, just write the message to it
            Ndef ndef = Ndef.get(tag);
            if(ndef != null) {
                ndef.connect();

                // Make sure the tag is writable
                if(!ndef.isWritable()) {
                    DialogUtils.displayErrorDialog(context, R.string.nfcReadOnlyErrorTitle, R.string.nfcReadOnlyError);
                    return false;
                }

                // Check if there's enough space on the tag for the message
                int size = message.toByteArray().length;
                if(ndef.getMaxSize() < size) {
                    DialogUtils.displayErrorDialog(context, R.string.nfcBadSpaceErrorTitle, R.string.nfcBadSpaceError);
                    return false;
                }

                try {
                    // Write the data to the tag
                    ndef.writeNdefMessage(message);
                    
                    DialogUtils.displayInfoDialog(context, R.string.nfcWrittenTitle, R.string.nfcWritten);
                    return true;
                } catch (TagLostException tle) {
                    DialogUtils.displayErrorDialog(context, R.string.nfcTagLostErrorTitle, R.string.nfcTagLostError);
                    return false;
                } catch (IOException ioe) {
                    DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                    return false;
                } catch (FormatException fe) {
                    DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                    return false;
                }
            // If the tag is not formatted, format it with the message
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if(format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        
                        DialogUtils.displayInfoDialog(context, R.string.nfcWrittenTitle, R.string.nfcWritten);
                        return true;
                    } catch (TagLostException tle) {
                        DialogUtils.displayErrorDialog(context, R.string.nfcTagLostErrorTitle, R.string.nfcTagLostError);
                        return false;
                    } catch (IOException ioe) {
                        DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                        return false;
                    } catch (FormatException fe) {
                        DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                        return false;
                    }
                } else {
                    DialogUtils.displayErrorDialog(context, R.string.nfcNoNdefErrorTitle, R.string.nfcNoNdefError);
                    return false;
                }
            }
        } catch(Exception e) {
            DialogUtils.displayErrorDialog(context, R.string.nfcUnknownErrorTitle, R.string.nfcUnknownError);
        }

        return false;
    }

}
