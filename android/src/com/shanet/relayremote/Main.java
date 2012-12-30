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

import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

public class Main extends FragmentActivity {
	
	private RelaysFragment relaysFrag;
	private RelayGroupsFragment relayGroupsFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		RelayPagerAdapter pagerAdapter = new RelayPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter);
		
		// Show the welcome or changelog dialog if necessary
		Utils.showOpeningDialogs(this);
		
		// Init the fragments
		relaysFrag = new RelaysFragment();
		relayGroupsFrag = new RelayGroupsFragment();
	}
	
	
	public class RelayPagerAdapter extends FragmentPagerAdapter {

		public RelayPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public Fragment getItem(int position) {			
			switch(position) {
				case 0:
					return relaysFrag;
				case 1:
					return relayGroupsFrag;
				default:
					return null;
			}
		}

		public int getCount() {
			return 2;
		}

		public CharSequence getPageTitle(int position) {
			switch(position) {
				case 0:
					return getString(R.string.relays);
				case 1:
					return getString(R.string.groups);
			}
			return null;
		}
	}
	
	
	public RelaysFragment getRelaysFrag() {
		return relaysFrag;
	}
	
	
	public RelayGroupsFragment getRelayGroupsFrag() {
		return relayGroupsFrag;
	}
	
	
	public void updateRelaysAndGroupsStates(boolean reloadFromDatabase) {
		Toast.makeText(this, R.string.refreshingRelays, Toast.LENGTH_SHORT).show();

		// We may not want to check the database to save some time so only do so if requested
		if(reloadFromDatabase) {
			relaysFrag.reloadRelays();
			relayGroupsFrag.reloadGroups();
		}
		
		// Update the relays and groups (the relay frag update function will call the groups update function
		// when the background threads have gotten the relay states from the servers)
		relaysFrag.updateRelayStates();
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// If the activity is the edit relay/group activity, we should update the relays
		if(requestCode == Constants.ADD_EDIT_CODE) {
			updateRelaysAndGroupsStates(true);
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
