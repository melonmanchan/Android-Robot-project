package com.example.robotapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Settings extends ActionBarActivity {

	ApplicationStateManager appState;

	
	ListView deviceList;
	
	private BluetoothAdapter bluetoothAdapter;
	private Set<BluetoothDevice> pairedDevices;	
	private ArrayList<BluetoothDevice> foundDevices;
    private ArrayAdapter<String> BTArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		appState = ((ApplicationState)getApplicationContext()).getStateManager();
		initiateReferencesFromAppState();
		deviceList = (ListView) findViewById(R.id.deviceList);		
		BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		List<String> s = new ArrayList<String>();
		for(BluetoothDevice bt : pairedDevices) {  
		
    	BTArrayAdapter.add(bt.getName() + "\n" + bt.getAddress());
		}
		deviceList.setAdapter(BTArrayAdapter);

    	BTArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initiateReferencesFromAppState() {
		System.out.println("initiateReferencesFromAppState");
		this.bluetoothAdapter = appState.getBluetoothAdapter();
		//this.bluetoothManager = appState.getBluetoothManager();
		this.pairedDevices = appState.getPairedDevices();
		this.foundDevices = appState.getFoundDevices();
		

	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	System.out.println("making receive");
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            System.out.println("Found new device: " + device.getName() + " | " + device.getAddress());
	            Toast.makeText(getApplicationContext(), "Found new device: " + device.getName() + " | " + device.getAddress(), Toast.LENGTH_LONG).show();
	            if (!foundDevices.contains(device)) {
	            	foundDevices.add(device);
	            }
	        }
	    }
	};

	public void discoverBluetoothDevices(View view)
	{
		try {
		   System.out.println("Discovering new bluetooth devices");
			   if (bluetoothAdapter.isDiscovering()) {
				   System.out.println("Startdiscovery");
				   // the button is pressed when it discovers, so cancel the discovery
				   bluetoothAdapter.cancelDiscovery();
			   }
			   else {
				  
				   bluetoothAdapter.startDiscovery();
					 System.out.println("Receive start");

					registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));	
			 System.out.println("Receive donedsds");
			   }
			   
		}
		catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
		}
		   
	}
	
}
