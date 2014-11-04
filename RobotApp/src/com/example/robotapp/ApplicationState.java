package com.example.robotapp;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ApplicationState extends Application {
	
	private final static int REQUEST_ENABLE_BT = 1;
	
	private String cameraIPAddress;
	private int cameraPort;
	
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private Set<BluetoothDevice> pairedDevices;
	private ArrayList<BluetoothDevice> foundDevices;
	
	public void onCreate() {
		super.onCreate();
		System.out.println("Application class onCreate");
		foundDevices = new ArrayList<BluetoothDevice>();
	}
	
	// Getter-setter boilerplate
	public String getCameraIPAddress() {
		return cameraIPAddress;
	}
	
	public void setCameraIPAddress(String cameraIPAddress) {
		this.cameraIPAddress = cameraIPAddress;
	}
	public int getCameraPort() {
		return cameraPort;
	}
	public void setCameraPort(int cameraPort) {
		this.cameraPort = cameraPort;
	}
	public BluetoothManager getBluetoothManager() {
		return bluetoothManager;
	}
	public void setBluetoothManager(BluetoothManager bluetoothManager) {
		this.bluetoothManager = bluetoothManager;
	}
	public BluetoothAdapter getBluetoothAdapter() {
		return bluetoothAdapter;
	}
	public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
		this.bluetoothAdapter = bluetoothAdapter;
	}
	public Set<BluetoothDevice> getPairedDevices() {
		return pairedDevices;
	}
	public void setPairedDevices(Set<BluetoothDevice> pairedDevices) {
		this.pairedDevices = pairedDevices;
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
		    //this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    this.startActivity(enableBtIntent);
		    //Feed.this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
	
	
}
