package com.example.robotapp;

import java.io.OutputStream;
import java.util.Stack;


public class BluetoothStreamManager {

	private OutputStream outputStream;
	private Stack<String> commandStack;
	
	public BluetoothStreamManager()
	{
		outputStream = null;
		commandStack = new Stack<String>();
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
