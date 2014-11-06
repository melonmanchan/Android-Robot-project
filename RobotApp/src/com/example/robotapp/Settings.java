package com.example.robotapp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Settings extends ActionBarActivity {

	private final static int REQUEST_ENABLE_BT = 1;
	private final static UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private String cameraIPAddress;
	private int cameraPort;
 
	private ApplicationState appState;
	private BluetoothStreamManager btStream;
	ListView deviceList;
	
	private BluetoothAdapter bluetoothAdapter;
	//private Set<BluetoothDevice> pairedDevices;	
	//private ArrayList<BluetoothDevice> foundDevices;
    private ArrayAdapter<String> BTArrayAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice deviceInUse;
    
    private OutputStream bluetoothOutput;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		appState = (ApplicationState)this.getApplication();
		btStream = appState.getStateManager();
		
		deviceList = (ListView) findViewById(R.id.deviceList);		
		BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		bluetoothDevices = new ArrayList<BluetoothDevice>();
		initiateBluetooth();
		refreshDeviceList();
		
		
		deviceList.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
		{
			String selectedItem = (String) (deviceList.getItemAtPosition(position));
			//String address = selectedItem.substring(selectedItem.length()-17);
			String address = selectedItem.split("\\r?\\n")[1];
		    Toast.makeText(getApplicationContext(), address, Toast.LENGTH_LONG).show();
		    connectToDevice(address);
		}
		});
		
		
	}


	private void refreshDeviceList()
	{
		BTArrayAdapter.clear();
		List<String> s = new ArrayList<String>();
		for(BluetoothDevice bt : bluetoothDevices) {  
		
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
	
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	System.out.println("making receive");
	        String action = intent.getAction();
	        // When discovery finds a device
	        System.out.println("lel");
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            if (!bluetoothDevices.contains(device)) {
	            // Add the name and address to an array adapter to show in a ListView
	            System.out.println("Found new device: " + device.getName() + " | " + device.getAddress());
	            Toast.makeText(getApplicationContext(), "Found new device: " + device.getName() + " | " + device.getAddress(), Toast.LENGTH_LONG).show();
	            bluetoothDevices.add(device);
	            refreshDeviceList();
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
	
	private void connectToDevice(String address)
	{
		try {
			deviceInUse = bluetoothAdapter.getRemoteDevice(address);
			bluetoothAdapter.cancelDiscovery();
			
			bluetoothSocket = deviceInUse.createRfcommSocketToServiceRecord(APP_UUID);
			
			bluetoothSocket.connect();
			System.out.println("Opened socket for device " + deviceInUse.getName());
			bluetoothOutput = bluetoothSocket.getOutputStream();
			
			btStream.setOutputStream(bluetoothOutput);
			
		}
		catch(Exception ex)
		{
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void initiateBluetooth() {
		try {
			
		System.out.println("Initiating bluetooth...");
	
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		System.out.println("checking if adapter is null...");
		
		if (bluetoothAdapter == null)
		{
			Toast.makeText(this, "Device does not support bluetooth :(", Toast.LENGTH_LONG).show();
			System.exit(0);
		}
		
		System.out.println("checking if adapter is enabled...");

		
		if (!bluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    //Feed.this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		System.out.println("getting bonded devices...");

		// Find already bound device
		bluetoothDevices.addAll(bluetoothAdapter.getBondedDevices());
		
		System.out.println("making le toast...");

		System.out.println("Found bluetooth devices: " + bluetoothDevices.size());

		for (BluetoothDevice bt : bluetoothDevices)
		{
			System.out.println("Old device name: " + bt.getName() + " , and address: " + bt.getAddress());
			//foundDevices.add(bt);
		}
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}
	
	public void switchToFeed(View view)
	{
		if (btStream.getInputStream() == null)
		{
			Toast.makeText(getApplicationContext(), "You must have a valid bluetooth device to continue!", Toast.LENGTH_LONG).show();
			return;
		}
		btStream.workThread.start();
		Intent intent = new Intent(getApplicationContext(), Feed.class);
		startActivity(intent);
	}
	
	protected void onDestroy() {
		super.onDestroy();
		try {
		if (mReceiver != null)
		{
			unregisterReceiver(mReceiver);
		}
		}
		catch(Exception ex)
		{
			
		}
	}
	
}
