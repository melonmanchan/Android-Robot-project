package com.example.robotapp;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


// Must extend application class to handle passing complex stream objects between activities. Serializabe/Parcelable not an option.
public class ApplicationState extends Application {
	private BluetoothStreamManager stateManager = new BluetoothStreamManager();
	
	
	public BluetoothStreamManager getStateManager() {
		return stateManager;
	}
	
    @Override
    public void onCreate() {
        super.onCreate();
        stateManager = new BluetoothStreamManager();

    }

	
}
