package com.example.robotapp;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.camera.simplemjpeg.*;
public class Feed extends ActionBarActivity {

	
	private boolean motorStateChanged = false;
	private boolean servoStateChanged = false; 
	private boolean isTransmittingMessage = false;;

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
	
	// Byte array to store all necessary value sin
	private byte[] motorCommand;
	
	// Handlers and runnable responsible for sending data to the Arduino
	private Handler movementHandler;
	private Runnable movementRunnable;
	
	// Options, received from Settings activity
	private int movementUpdateSpeed;
	private String videoIpAddress = "192.168.0.5";
	private String videoPort = "5000";
	private boolean isCameraEnabled = true;
	
	// Two joysticks to control movement
	private JoystickView leftJoystick;
	private JoystickView rightJoystick;
	private MjpegView videoFeed = null;
	
	private ApplicationState appState;
	private BluetoothStreamManager btStreamManager;
		
	 // F = 70
	 // S = 83
	 // R = 82
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras == null) {
				movementUpdateSpeed = 120;
				System.out.println("null");
			}
			else {
				movementUpdateSpeed = extras.getInt("BT_UPDATE_SPEED");
				videoIpAddress = extras.getString("CAMERA_IP_ADDRESS");
				videoPort = extras.getString("CAMERA_PORT");
				isCameraEnabled = extras.getBoolean("IS_CAMERA_ENABLED");

			}
		}
		System.out.println(videoIpAddress +videoPort );
		appState = (ApplicationState)this.getApplication();
		btStreamManager = appState.getStateManager();
		btStreamManager.setCurrentActivity(this);
		leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
		rightJoystick = (JoystickView) findViewById(R.id.rightJoystick);
		
		System.out.println("Initiating  motorcommand!");
		
		motorCommand = new byte[6];
		motorCommand[0] = MOTOR_COMMAND_DELIMITER; // ASCII character { which is the starting delimiter to tell the Robot that there's a motor movement command incoming!
		motorCommand[1] = MOTOR_RELEASE; // second byte indicates the speed of the left motor.
		motorCommand[2] = 0; // no motor movement
		motorCommand[3] = MOTOR_RELEASE; // 3rd byte indicates speed of right motor
		motorCommand[4] = 0;// no motor movement
	    motorCommand[5] = SERVO_NOTHING; // no servo movement
		System.out.println("Done initianting motorcommand!");
	    
		initiateMovementHandlers();
		initiateJoystickListeners();
		
		if (isCameraEnabled == true)
		{
			videoFeed = (MjpegView) findViewById(R.id.videoFeed);
			fetchVideoFeed();
		}

	}

	@Override
	protected void onResume(){
		super.onResume();
		System.out.println("changed activity");
		btStreamManager.setCurrentActivity(this);
		movementHandler.postDelayed(movementRunnable, movementUpdateSpeed);
		if (videoFeed != null)
		{
			videoFeed.resumePlayback();
		}
	}
	
	protected void onPause() {
		super.onPause();
		if (videoFeed != null)
		{
			videoFeed.stopPlayback();
		}
		movementHandler.removeCallbacks(movementRunnable);
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		movementHandler.removeCallbacks(movementRunnable);
	}
	
	protected void onDestroy() {
		
		if (videoFeed != null)
		{
			videoFeed.freeCameraMemory();
		}
		super.onDestroy();
	}
	
	private void initiateMovementHandlers() {
		
		movementHandler = new Handler();
		movementRunnable = new Runnable() {
			public void run() {
				// don't send motor command while message command is still transmitting
				if (!isTransmittingMessage)
				{
					// motorCommand to temp variable so it doesn't change while the function is not yet finished
					byte[] temp = motorCommand;
					
		
					if (servoStateChanged == false)
					{
						temp[5] = SERVO_NOTHING;
					}
					
					if (motorStateChanged == false)
					{
						temp[1] = MOTOR_RELEASE; // second byte indicates the speed of the left motor.
						temp[2] = 0; // no motor movement
						temp[3] = MOTOR_RELEASE;
						temp[4] = 0;// no motor movement
					}
					
					btStreamManager.push(temp);
					
					servoStateChanged = false;
					motorStateChanged = false;
				
				movementHandler.postDelayed(movementRunnable, movementUpdateSpeed);
				
				}
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
            	motorStateChanged = true;

            	if (power > 100) {
            		power = 100;
            	}
            	
            	if (direction == JoystickView.FRONT || direction == JoystickView.BOTTOM || direction == JoystickView.LEFT || direction == JoystickView.BOTTOM)
            	{
            		// axisforce is calculated as a sliding value of 0 to 127
            		axisForce = (byte) (byte)(127 * power/100);
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
                	motorCommand[2] = axisForce;
                	motorCommand[3] = MOTOR_FORWARD;
                	motorCommand[4] = (byte) (axisForce / 2);
                	
                    break;
                case JoystickView.RIGHT:
                //	System.out.println("M: right");

                	// multiply axisforce by 0.7, since otherwise the robot spins impossibly fast!
                	motorCommand[1] = MOTOR_FORWARD;
                	motorCommand[2] = (byte) (axisForce * 0.7);
                	motorCommand[3] = MOTOR_BACKWARD;
                	motorCommand[4] = (byte) (axisForce * 0.7);
                	
                    break;
                case JoystickView.RIGHT_BOTTOM:
                	//System.out.println("M: right-bottom");

                	motorCommand[1] = MOTOR_BACKWARD;
                	motorCommand[2] = axisForce;
                	motorCommand[3] = MOTOR_BACKWARD;
                	motorCommand[4] = (byte) (axisForce / 2);
                	
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
                	motorCommand[2] = (byte) (axisForce / 2);
                	motorCommand[3] = MOTOR_BACKWARD;
                	motorCommand[4] = axisForce;
                    break;
                case JoystickView.LEFT:
                	//System.out.println("M: left");

                	motorCommand[1] = MOTOR_BACKWARD;
                	motorCommand[2] = (byte) (axisForce * 0.7);
                	motorCommand[3] = MOTOR_FORWARD;
                	motorCommand[4] = (byte) (axisForce * 0.7);
                	
                    break;
                case JoystickView.LEFT_FRONT:
                	//System.out.println("M: left-front");

                	motorCommand[1] = MOTOR_FORWARD;
                	motorCommand[2] = (byte) (axisForce / 2);
                	motorCommand[3] = MOTOR_FORWARD;
                	motorCommand[4] = axisForce;
                	
                    break;
                default:
                	
                	motorCommand[1] = MOTOR_RELEASE;
                	motorCommand[2] = 0;
                	motorCommand[3] = MOTOR_RELEASE;
                	motorCommand[4] = 0;
                }
        

            }
        }, movementUpdateSpeed / 2 ); 
	
		
		rightJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            public void onValueChanged(int angle, int power, int direction) {
            	servoStateChanged = true;
                // TODO Auto-generated method stub
                //angleTextView.setText(" " + String.valueOf(angle) + "°");
                //powerTextView.setText(" " + String.valueOf(power) + "%");
            	//System.out.println("angle: " + angle + " power: " + power);
            	
                switch (direction) {
                case JoystickView.FRONT:
                	 motorCommand[5] = SERVO_UP;
                    break;
                    
                case JoystickView.FRONT_RIGHT:
                	 motorCommand[5] = SERVO_UP_RIGHT;
                    break;
                    
                case JoystickView.RIGHT:
                	 motorCommand[5] = SERVO_RIGHT;
                    break;
                    
                case JoystickView.RIGHT_BOTTOM:
                	 motorCommand[5] = SERVO_DOWN_RIGHT;
                    break;
                    
                case JoystickView.BOTTOM:
                	 motorCommand[5] = SERVO_DOWN;
                    break;
                    
                case JoystickView.BOTTOM_LEFT:
                	 motorCommand[5] = SERVO_DOWN_LEFT;
                    break;
                    
                case JoystickView.LEFT:
                	 motorCommand[5] = SERVO_LEFT;
                    break;
                    
                case JoystickView.LEFT_FRONT:
                	 motorCommand[5] = SERVO_UP_LEFT;
                    break;
                    
                default:
                	 motorCommand[5] = SERVO_NOTHING;
                }
            }
        }, movementUpdateSpeed / 2); 
		
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    switch(keycode) {
	        case KeyEvent.KEYCODE_MENU:
	    		if (videoFeed != null)
	    		{
	    			videoFeed.stopPlayback();
	    		}
	    		movementHandler.removeCallbacks(movementRunnable);
	            Intent menuIntent = new Intent(this, Settings.class);
	            startActivity(menuIntent);
	            return true;
	    }

	    return super.onKeyDown(keycode, e);
	}
	
	public void openMessagePrompt(View view)
	{
		
		final EditText messageEditText = new EditText(this);
		
		// Build a dialog box to get user input
		new AlertDialog.Builder(this)
	    .setTitle("Message")
	    .setMessage("Make the robot talk!")
	    .setView(messageEditText)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            String value = messageEditText.getText().toString();
	            // regex magic to check for ASCII compliance.
	            if (!value.matches("\\p{ASCII}+"))
	            {
	            	Toast.makeText(getApplicationContext(), "ASCII only please!", Toast.LENGTH_LONG).show();
	            }
	            else {
	            	
	            	sendMessageToRobot(value);
	            }
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        	return;
	        }
	    }).show();
		
	}
 
	private void sendMessageToRobot(String message)
	{
		isTransmittingMessage = true;
		// "z" is the ascii equivelant of 123, which is the message starting delimiter. "\n" is the ending delimiter.
		message.replace('\n', ' ');
		message = "z" + message + "\n";
		try {
			byte[] messageBytes = message.getBytes("US-ASCII");
			btStreamManager.push(messageBytes);
			Thread.sleep(message.length() * 150);
			isTransmittingMessage = false;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(), "Ascii only, please!", Toast.LENGTH_SHORT).show();
		}
		catch (InterruptedException e) {
			
		}
				
	}
	

	
	public void fetchVideoFeed()
	{
		new DoRead().execute( videoIpAddress, videoPort);
	}
	
	
    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
    	protected MjpegInputStream doInBackground( String... params){
    		Socket socket = null;
    		try {
				socket = new Socket( params[0], Integer.valueOf( params[1]));
	    		return (new MjpegInputStream(socket.getInputStream()));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}
    		return null;
    	}
    	
        protected void onPostExecute(MjpegInputStream result) {
        	videoFeed.setSource(result);
           if(result!=null)
           {
        	   // skip every other frame, for better speed and less lag
            	result.setSkip(2);
           }
           videoFeed.setDisplayMode(MjpegView.SIZE_BEST_FIT);
           videoFeed.showFps(true);
        }
    }
	
	
}
