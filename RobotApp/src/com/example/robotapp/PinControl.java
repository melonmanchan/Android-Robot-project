package com.example.robotapp;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class PinControl extends ActionBarActivity {

	
	private static byte PIN_TOGGLE_DELIMITER = 121;
	private static byte PIN_PWM_DELIMITER = 120;
	private byte[] toggleCommand;

	private byte[] pwmCommand;
	
	private ApplicationState appState;
	private BluetoothStreamManager btStreamManager;
	
	private SeekBar Pin11SeekBar;
	private SeekBar Pin6SeekBar;
	private SeekBar Pin5SeekBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin_control);
		
		toggleCommand = new byte[3];
		pwmCommand = new byte[3];
		
		toggleCommand[0] = PIN_TOGGLE_DELIMITER;
		pwmCommand[0] = PIN_PWM_DELIMITER;
		
		appState = (ApplicationState)this.getApplication();
		btStreamManager = appState.getStateManager();
		btStreamManager.setCurrentActivity(this);

		Pin11SeekBar = (SeekBar) findViewById(R.id.pin11SeekBar);
		Pin6SeekBar = (SeekBar) findViewById(R.id.pin6SeekBar);
		Pin5SeekBar = (SeekBar) findViewById(R.id.pin5SeekBar);

		Pin11SeekBar.setOnSeekBarChangeListener(new PWMToggleListener());
		Pin6SeekBar.setOnSeekBarChangeListener(new PWMToggleListener());
		Pin5SeekBar.setOnSeekBarChangeListener(new PWMToggleListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pin_control, menu);
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

	public void pinToggleButtonClick(View view)
	{
		
		boolean isOn = ((ToggleButton) view).isChecked();
		
		if (isOn)
			toggleCommand[2] = 1;
		else
			toggleCommand[2] = 0;
		
		
		switch (view.getId()){
		case R.id.ArduinoPin4:
			toggleCommand[1] = 4;
			break;
			
		case R.id.ArduinoPin5:
			toggleCommand[1] = 5;

			break;
			
		case R.id.ArduinoPin6:
			toggleCommand[1] = 6;

			break;
			
		case R.id.ArduinoPin7:
			toggleCommand[1] = 7;

			break;
			
		case R.id.ArduinoPin8:
			toggleCommand[1] = 8;

			break;
			
		case R.id.ArduinoPin11:
			toggleCommand[1] = 11;

			break;
			
		case R.id.ArduinoPin12:
			toggleCommand[1] = 12;

			break;
			
		case R.id.ArduinoPin13:
			toggleCommand[1] = 13;

			break;
		}
		
		btStreamManager.push(toggleCommand);
	}
		
	private class PWMToggleListener implements SeekBar.OnSeekBarChangeListener 
	{
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			switch(seekBar.getId())
			{
			case R.id.pin5SeekBar:
				pwmCommand[1] = 5;
				break;
			case R.id.pin6SeekBar:
				pwmCommand[1] = 6;
				break;
			case R.id.pin11SeekBar:
				pwmCommand[1] = 11;
				break;
			}
			pwmCommand[2] = (byte) progress;
						
			btStreamManager.push(pwmCommand);

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			/*
			// TODO Auto-generated method stub
			switch(seekBar.getId())
			{
			case R.id.pin5SeekBar:
				pwmCommand[1] = 5;
				break;
			case R.id.pin6SeekBar:
				pwmCommand[1] = 6;
				break;
			case R.id.pin11SeekBar:
				pwmCommand[1] = 11;
				break;
			}
			pwmCommand[2] = (byte) seekBar.getProgress();
			
			System.out.println(pwmCommand[0] + " " + pwmCommand[1] + " " + pwmCommand[2]);
			
			btStreamManager.push(pwmCommand);*/
		}
		
	}
	
}
