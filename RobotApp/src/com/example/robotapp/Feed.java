package com.example.robotapp;



import android.support.v7.app.ActionBarActivity;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Feed extends ActionBarActivity {

	
	// Movement commands are sent to arduino as a byte array. first byte of array is delimiter, 2nd is left motor direction, 3rd is left motor speed
	// 4th is right motor direction, 5th is right motor speed, 6th is servo movement.
	private static byte MOTOR_COMMAND_DELIMITER = 123;
	
	private static byte MOTOR_FORWARD = 70;
	private static byte MOTOR_RELEASE = 82;
	private static byte MOTOR_BACKWARD = 66  ;
	
	// bytes indicating servo movement.
	private static byte SERVO_UP = 1;
	private static byte SERVO_UP_RIGHT = 2;
	private static byte SERVO_RIGHT = 3;
	private static byte SERVO_DOWN_RIGHT = 4;
	private static byte SERVO_DOWN = 5;
	private static byte SERVO_DOWN_LEFT = 6;
	private static byte SERVO_LEFT = 7;
	private static byte SERVO_UP_LEFT = 8;
	private static byte SERVO_NOTHING = 9;
	
	// Byte indicating the speed the motor should run, on axis up, down, left and right.
	private byte axisForce;
	
	private Handler movementHandler;
	private Runnable movementRunnable;
	
	private int movementUpdateSpeed = 1000;
	
	private JoystickView leftJoystick;
	private JoystickView rightJoystick;
	
	private ApplicationState appState;
	private BluetoothStreamManager btStream;

	private byte[] motorCommand;
	
	 // F = 70
	 // S = 83
	 // R = 82
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		appState = (ApplicationState)this.getApplication();
		btStream = appState.getStateManager();
		btStream.setCurrentActivity(this);
		leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
		rightJoystick = (JoystickView) findViewById(R.id.rightJoystick);
		
		System.out.println("Initiating  motorcommand!");
		
		motorCommand = new byte[6];
		motorCommand[0] = MOTOR_COMMAND_DELIMITER; // ASCII character { which is the starting delimiter to tell the Robot that there's a motor movement command incoming!
		motorCommand[1] = -128; // second byte indicates the speed of the left motor. -128 is 0 speed.
		motorCommand[2] = MOTOR_RELEASE; // no motor movement
		motorCommand[3] = -128;
		motorCommand[4] = MOTOR_RELEASE;// no motor movement
	    motorCommand[5] = SERVO_NOTHING; // no servo movement
		System.out.println("Done initianting motorcommand!");
	    
		initiateMovementHandlers();
		initiateJoystickListeners();
		
	}

	@Override
	protected void onResume(){
		super.onResume();
		System.out.println("changed activity");
		btStream.setCurrentActivity(this);
	}
	
	
	private void initiateMovementHandlers() {
		
		movementHandler = new Handler();
		movementRunnable = new Runnable() {
			public void run() {
				btStream.push(motorCommand);
				movementHandler.postDelayed(movementRunnable, movementUpdateSpeed);
			}
		};
		movementHandler.postDelayed(movementRunnable, movementUpdateSpeed);
		
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
            	if (power > 100) {
            		power = 100;
            	}
            	
            	if (direction == JoystickView.FRONT || direction == JoystickView.BOTTOM || direction == JoystickView.LEFT || direction == JoystickView.BOTTOM)
            	{
            		// axisforce is calculated as a sliding value of -127 to 128.
            		axisForce = (byte) ((byte) -128 + (byte)(255 * power/100));
            	}
            	
                switch (direction) {
                case JoystickView.FRONT:
                	                	                	
                	motorCommand[1] = MOTOR_FORWARD;
                	motorCommand[2] = axisForce;
                	motorCommand[3] = MOTOR_FORWARD;
                	motorCommand[4] = axisForce;
                	
                    break;
                case JoystickView.FRONT_RIGHT:
                	//System.out.println("M: up-right");

                	motorCommand[1] = MOTOR_FORWARD;
                	motorCommand[3] = MOTOR_FORWARD;
                	
                    break;
                case JoystickView.RIGHT:
                //	System.out.println("M: right");

                	
                	motorCommand[1] = MOTOR_FORWARD;
                	motorCommand[2] = axisForce;
                	motorCommand[3] = MOTOR_BACKWARD;
                	motorCommand[4] = axisForce;
                	
                    break;
                case JoystickView.RIGHT_BOTTOM:
                	//System.out.println("M: right-bottom");

                	motorCommand[1] = MOTOR_BACKWARD;
                	motorCommand[3] = MOTOR_BACKWARD;
                	
                    break;
                case JoystickView.BOTTOM:
                	//ystem.out.println("M: bottom");
                	motorCommand[1] = MOTOR_BACKWARD;
                	motorCommand[2] = axisForce;
                	motorCommand[3] = MOTOR_BACKWARD;
                	motorCommand[4] = axisForce;
                    break;
                case JoystickView.BOTTOM_LEFT:
                	//
                	motorCommand[1] = MOTOR_BACKWARD;
                	motorCommand[3] = MOTOR_BACKWARD;
                	
                    break;
                case JoystickView.LEFT:
                	//System.out.println("M: left");

                	motorCommand[1] = MOTOR_BACKWARD;
                	motorCommand[2] = axisForce;
                	motorCommand[3] = MOTOR_FORWARD;
                	motorCommand[4] = axisForce;
                	
                    break;
                case JoystickView.LEFT_FRONT:
                	//System.out.println("M: left-front");

                	motorCommand[1] = MOTOR_FORWARD;
                	motorCommand[3] = MOTOR_FORWARD;
                	
                    break;
                default:
                	//
                	motorCommand[1] = MOTOR_RELEASE;
                	motorCommand[2] = -127;
                	motorCommand[3] = MOTOR_RELEASE;
                	motorCommand[4] = -127;
                	
                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL); 
	
		
		rightJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            public void onValueChanged(int angle, int power, int direction) {
                // TODO Auto-generated method stub
                //angleTextView.setText(" " + String.valueOf(angle) + "°");
                //powerTextView.setText(" " + String.valueOf(power) + "%");
            	//System.out.println("angle: " + angle + " power: " + power);
                switch (direction) {
                case JoystickView.FRONT:
                	//System.out.println("S: front");

                	 motorCommand[5] = SERVO_UP;
                    break;
                case JoystickView.FRONT_RIGHT:
                	//System.out.println("S: front-right");

                	 motorCommand[5] = SERVO_UP_RIGHT;
                    break;
                case JoystickView.RIGHT:
                	//System.out.println("S: right");

                	 motorCommand[5] = SERVO_RIGHT;
                    break;
                case JoystickView.RIGHT_BOTTOM:
                	//System.out.println("S: right_bottom");

                	 motorCommand[5] = SERVO_DOWN_RIGHT;
                    break;
                case JoystickView.BOTTOM:
                	//System.out.println("S: bottom");

                	 motorCommand[5] = SERVO_DOWN;
                    break;
                case JoystickView.BOTTOM_LEFT:
                	//System.out.println("S: bottom_left");

                	 motorCommand[5] = SERVO_DOWN_LEFT;
                    break;
                case JoystickView.LEFT:
                	//System.out.println("S: left");

                	 motorCommand[5] = SERVO_LEFT;
                    break;
                case JoystickView.LEFT_FRONT:
                	//System.out.println("S: left-front");

                	 motorCommand[5] = SERVO_UP_LEFT;
                    break;
                default:
                	 motorCommand[5] = SERVO_NOTHING;
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
	
	
}
