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
	private ApplicationStateManager stateManager = new ApplicationStateManager(this);
	
	
	public ApplicationStateManager getStateManager() {
		return stateManager;
	}
	
    @Override
    public void onCreate() {
        super.onCreate();

        stateManager = new ApplicationStateManager(this.getApplicationContext());

    }

	
}
