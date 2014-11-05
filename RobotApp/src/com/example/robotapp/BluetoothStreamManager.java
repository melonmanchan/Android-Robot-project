package com.example.robotapp;

import java.io.OutputStream;
import java.util.Stack;

public class BluetoothStreamManager {
    // Stream for writing bytes to bluetooth
	private OutputStream outputStream;
	// Stack data structure to hold robot commands in string form
	private Stack<String> commandStack;
	// Seperate thread to keep running in background during the whole she-bang, running new commands if necessary. Thread is best implementation of
	// threading in Android for this use-case imo. AsyncTask too bulky, only for "short" asynchronous tasks. Don't see a need to update UI thread from here.
	// Thread seems to be the fastest implementation
	public Thread workThread;
	
	public BluetoothStreamManager()
	{
		outputStream = null;
		commandStack = new Stack<String>();
		workThread = new Thread()
		{
			@Override
		    public void run() {
			       android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			        try 
			    	{
				        		while(!Thread.interrupted())
				        		{
									if (!commandStack.isEmpty() && outputStream != null)
									{
											byte[] msgBuffer = commandStack.pop().getBytes("US-ASCII");
											//outputStream.write(msgBuffer);
											System.out.println("new buffer incoming!");
											for (int i = 0; i < msgBuffer.length; i++)
											{
												System.out.println("byte: " + msgBuffer[i]);
											}
											
											Thread.sleep(300);
						        	}
									
				        		}
							
					}
			        
			    	catch (Exception ex) 
			    	{
			    		System.out.println("BluetoothStreammanager exception:" + ex.getMessage());
			    	}
			}
			
		};
	}
	
	public OutputStream getInputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public Stack<String> getCommandStack() {
		return commandStack;
	}

	public void setCommandStack(Stack<String> commandStack) {
		this.commandStack = commandStack;
	}

	public void push(String command) {
		commandStack.push(command);
	}
	
	public String peek() {
		return commandStack.peek();
	}


}
/*
public class BluetoothStreamManager implements Parcelable {

	private OutputStream outputStream;
	private Stack<String> commandStack;
	
	public BluetoothStreamManager()
	{
		outputStream = null;
		commandStack = new Stack<String>();
	}
	
	public BluetoothStreamManager(OutputStream outputStream)
	{
		this.outputStream = outputStream;
	}
	
	public OutputStream getInputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public Stack<String> getCommandStack() {
		return commandStack;
	}

	public void setCommandStack(Stack<String> commandStack) {
		this.commandStack = commandStack;
	}

	public void push(String command) {
		commandStack.push(command);
	}
	
	public String peek() {
		return commandStack.peek();
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	 	dest.writeValue(outputStream);
	 	dest.writeValue(commandStack);
	}

	
	    public static final Parcelable.Creator<BluetoothStreamManager> CREATOR = new Parcelable.Creator<BluetoothStreamManager>() {
	  public BluetoothStreamManager createFromParcel(Parcel in) {
	    return new BluetoothStreamManager(in);
	 }

	@Override
	public BluetoothStreamManager[] newArray(int size) {
		// TODO Auto-generated method stub
		return new BluetoothStreamManager[size];
	}

	    };
	    
	    private BluetoothStreamManager(Parcel in) {
	    	outputStream = (OutputStream) in.readValue(OutputStream.class.getClassLoader());
	    	commandStack = (Stack<String>) in.readValue(Stack.class.getClassLoader());
	    }

}*/
