package com.example.robotapp;

import java.util.ArrayList;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ApplicationStateManager {
private final static int REQUEST_ENABLE_BT = 1;
	
	private String cameraIPAddress;
	private int cameraPort;
	
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private Set<BluetoothDevice> pairedDevices;
	private ArrayList<BluetoothDevice> foundDevices;
	private Context context;
	
	
	public ApplicationStateManager(Context context) {
		this.context = context;
		//System.out.println("Application class onCreate. context: " + context.toString());
		foundDevices = new ArrayList<BluetoothDevice>();
		//pairedDevices = new Set<BluetoothDevice>();
		cameraIPAddress = "";
		cameraPort = 0;
		initiateBluetooth();
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
	
	public ArrayList<BluetoothDevice> getFoundDevices() {
		return foundDevices;
	}

	public void setFoundDevices(ArrayList<BluetoothDevice> foundDevices) {
		this.foundDevices = foundDevices;
	}

	public void initiateBluetooth() {
		try {
			
		System.out.println("Initiating bluetooth...");
	
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		System.out.println("checking if adapter is null...");
		
		if (bluetoothAdapter == null)
		{
			Toast.makeText(context, "Device does not support bluetooth :(", Toast.LENGTH_LONG).show();
			System.exit(0);
		}
		
		System.out.println("checking if adapter is enabled...");

		
		if (!bluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    //this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    context.startActivity(enableBtIntent);
		    //Feed.this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		System.out.println("getting bonded devices...");

		// Find already bound device
		pairedDevices = bluetoothAdapter.getBondedDevices();
		
		System.out.println("making le toast...");

		System.out.println("Found bluetooth devices: " + pairedDevices.size());

		for (BluetoothDevice bt : pairedDevices)
		{
			System.out.println("Old device name: " + bt.getName() + " , and address: " + bt.getAddress());
			foundDevices.add(bt);
		}
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}
	
	
}
