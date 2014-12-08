package com.example.robotapp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;

public class BluetoothStreamManager {
	
	// reference to current Activity
	private Activity currentActivity;
	
	private String deviceAddress;
	
	private BluetoothDevice bluetoothDevice;
    // Stream for writing bytes to bluetooth
	private OutputStream outputStream;
	// Queue data structure to hold robot commands in byte array form
	private ConcurrentLinkedQueue<byte[]> commandQueue;
		// Seperate thread to keep running in background during the whole she-bang, running new commands if necessary. Thread is best implementation of
	// threading in Android for this use-case imo. AsyncTask too bulky, only for "short" asynchronous tasks. Don't see a need to update UI thread from here.
	// Thread seems to be the fastest implementation
	public Thread workThread;
	
	public BluetoothStreamManager()
	{
		deviceAddress = "";
		outputStream = null;
		commandQueue = new ConcurrentLinkedQueue<byte[]>();
		workThread = new Thread()
		{
			@Override
		    public void run() {
			        try 
			    	{
				        		while(!Thread.interrupted())
				        		{
									if (!commandQueue.isEmpty() && outputStream != null)
									{
										//System.out.println("===============================");
											byte[] msgBuffer = commandQueue.poll();
											outputStream.write(msgBuffer);
											//System.out.println("new buffer incoming!");
											outputStream.flush();
											//System.out.println("===============================");

									}
									
				        		}
							
					}
			        catch (IOException ex) {
			        	System.out.println("IOError!");
			        	
			        	currentActivity.runOnUiThread(new Runnable() {
			        	    public void run() {
			        	    	
			        	       new AlertDialog.Builder(currentActivity).setTitle("Bluetooth failed!")
			        	       .setMessage("Fuck! The bluetooth connection has failed. Go back to settings and initiate a new connection?").setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			        	           public void onClick(DialogInterface dialog, int which) { 
			        	               // Go back to settings
			        	        	   Intent intent = new Intent(currentActivity, Settings.class);
			        	       		   currentActivity.startActivity(intent);
			        	        	   
			        	           }
			        	        })
			        	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        	           public void onClick(DialogInterface dialog, int which) { 
			        	               // do nothing
			        	        	   return ;
			        	           }
			        	        })
			        	       .setIcon(android.R.drawable.ic_dialog_alert)
			        	        .show();
			        	       
			        	    }
			        	});
			        }
			}
			
		};
	}
	
	public OutputStream getInputStream() 
	{
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) 
	{
		this.outputStream = outputStream;
	}

	public String getDeviceAddress()
	{
		return deviceAddress;
	}
	
	public void setDeviceAddress(String deviceAddress)
	{
		this.deviceAddress = deviceAddress;
	}
	
	public ConcurrentLinkedQueue<byte[]> getCommandStack() 
	{
		return commandQueue;
	}

	public void setCommandStack(ConcurrentLinkedQueue<byte[]> commandQueue) 
	{
		this.commandQueue = commandQueue;
	}

	public void push(byte[] command) 
	{
		commandQueue.add(command);
	}
	
	public byte[] peek() 
	{
		return commandQueue.peek();
	}

	public void setCurrentActivity(Activity activity) 
	{
		this.currentActivity = activity;
	}

	public void closeStream()
	{
		if(this.outputStream != null)
			try {
				this.outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
}
