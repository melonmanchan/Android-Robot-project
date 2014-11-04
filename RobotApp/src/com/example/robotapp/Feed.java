package com.example.robotapp;

import java.util.ArrayList;
import java.util.Set;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Feed extends ActionBarActivity {

	
	private static final int REQUEST_ENABLE_BT = 1;
	
	private JoystickView leftJoystick;
	//private JoystickView rightJoystick;
	private OtherJoystick rightJoystick;
	
	
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private Set<BluetoothDevice> pairedDevices;
	
	private ArrayList<BluetoothDevice> foundDevices;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		foundDevices = new ArrayList<BluetoothDevice>();
		
		leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
		//rightJoystick = (JoystickView) findViewById(R.id.RightJoystick);
		rightJoystick = (OtherJoystick) findViewById(R.id.otherJoystick1);
	
		initiateJoystickListeners();
		
		initiateBluetooth();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.feed, menu);
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
	
	public void initiateJoystickListeners()
	{
		leftJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            public void onValueChanged(int angle, int power, int direction) {
                // TODO Auto-generated method stub
                //angleTextView.setText(" " + String.valueOf(angle) + "°");
                //powerTextView.setText(" " + String.valueOf(power) + "%");
            	//System.out.println("angle: " + angle + " power: " + power);
            	System.out.println("x:" + Math.max(-100, Math.min(100, leftJoystick.xPosition)) + " y: " + Math.max(-100, Math.min(100, leftJoystick.yPosition)));
                switch (direction) {
                case JoystickView.FRONT:
                	
                    break;
                case JoystickView.FRONT_RIGHT:
                	
                    break;
                case JoystickView.RIGHT:
                	
                    break;
                case JoystickView.RIGHT_BOTTOM:
                	
                    break;
                case JoystickView.BOTTOM:
                	
                    break;
                case JoystickView.BOTTOM_LEFT:
                	
                    break;
                case JoystickView.LEFT:
                	
                    break;
                case JoystickView.LEFT_FRONT:
                	
                    break;
                default:
                	
                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL); 
		
	}
	
	
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    switch(keycode) {
	        case KeyEvent.KEYCODE_MENU:
	            Intent menuIntent = new Intent(this, Settings.class);
	            startActivity(menuIntent);
	            return true;
	    }

	    return super.onKeyDown(keycode, e);
	}
	
	public void initiateBluetooth() {
		
		System.out.println("Initiating bluetooth...");
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (bluetoothAdapter == null)
		{
			Toast.makeText(getApplicationContext(), "Device does not support bluetooth :(", Toast.LENGTH_LONG).show();
			System.exit(0);
		}
		
		if (!bluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		// Find already bound device
		pairedDevices = bluetoothAdapter.getBondedDevices();
		
		Toast.makeText(this, "Found bluetooth devices: " + pairedDevices.size(), Toast.LENGTH_LONG).show();

		for (BluetoothDevice bt : pairedDevices)
		{
			System.out.println("Old device name: " + bt.getName() + " , and address: " + bt.getAddress());
			foundDevices.add(bt);
		}
		
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            System.out.println("Found new device: " + device.getName() + " | " + device.getAddress());
	        }
	    }
	};

	public void discoverBluetoothDevices(View view)
	{
		try {
		   System.out.println("Discovering new bluetooth devices");
			   if (bluetoothAdapter.isDiscovering()) {
				   // the button is pressed when it discovers, so cancel the discovery
				   bluetoothAdapter.cancelDiscovery();
			   }
			   else {
				   
				   bluetoothAdapter.startDiscovery();
					
					registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));	
				}    
			   
		}
		catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG);
		}
		   
	}

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	
}
