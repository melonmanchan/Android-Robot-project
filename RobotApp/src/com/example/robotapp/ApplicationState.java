package com.example.robotapp;
import android.app.Application;

// Must extend application class to handle passing complex stream objects between activities. Serializabe/Parcelable not an option.
public class ApplicationState extends Application {
	private BluetoothStreamManager stateManager;
	
	
	public BluetoothStreamManager getStateManager() {
		return stateManager;
	}
	
    @Override
    public void onCreate() {
        super.onCreate();
        stateManager = new BluetoothStreamManager();
        stateManager.workThread.start();
    }

	
}
