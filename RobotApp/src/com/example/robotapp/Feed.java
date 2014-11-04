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

	ApplicationStateManager appState;
	
	
	private static final int REQUEST_ENABLE_BT = 1;
	
	private JoystickView leftJoystick;
	//private JoystickView rightJoystick;
	private JoystickView rightJoystick;
	
	
	/*private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private Set<BluetoothDevice> pairedDevices;
	
	private ArrayList<BluetoothDevice> foundDevices;
	*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		appState = ((ApplicationState)getApplicationContext()).getStateManager();
		
		
		//foundDevices = new ArrayList<BluetoothDevice>();
		
		leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
		//rightJoystick = (JoystickView) findViewById(R.id.RightJoystick);
		rightJoystick = (JoystickView) findViewById(R.id.rightJoystick);
	
		initiateJoystickListeners();
		
		initiateReferencesFromAppState();
		
	}

	private void initiateReferencesFromAppState() {
		System.out.println("initiateReferencesFromAppState");
		/*
		this.bluetoothAdapter = appState.getBluetoothAdapter();
		this.bluetoothManager = appState.getBluetoothManager();
		this.pairedDevices = appState.getPairedDevices();
		this.foundDevices = appState.getFoundDevices();*/
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
	
	

	protected void onDestroy() {
		super.onDestroy();
		//if (mReceiver != null)
		//{
			//unregisterReceiver(mReceiver);
		//}
	}
	
}
