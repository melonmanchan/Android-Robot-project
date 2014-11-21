#include <Arduino.h>
#include <Wire.h>
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include "SPI.h" // Comment out this line if using Trinket or Gemma
#include <SoftwareSerial.h>
#include <Adafruit_MotorShield.h>
#include <Servo2.h>

Adafruit_MotorShield motorShield = Adafruit_MotorShield();
Adafruit_DCMotor *leftMotor = motorShield.getMotor(3);
Adafruit_DCMotor *rightMotor = motorShield.getMotor(4);

Adafruit_8x8matrix eyematrix = Adafruit_8x8matrix();
SoftwareSerial mouthSerial(2, 3); // RX, TX


char robotMessage[40];
byte robotMessageIndex = 0;

Servo bottomServo;  // create servo object to control a servo 
Servo topServo; // a maximum of eight servo objects can be created 
 
byte bottomPos = 90;    // variable to store the servo position 
byte topPos = 150;


static const uint8_t matrixAddr = 0x71;

unsigned long eyeTimer;
unsigned long eyeLastTimer;
unsigned long eyeAcc;

unsigned long mouthTimer;
unsigned long mouthLastTimer;
unsigned long mouthAcc;

unsigned long servoMovementTimer;
unsigned long servoMovementLastTimer;
unsigned long servoMovementTimerAcc;


unsigned long messageStartTime;
unsigned long messageCurrentTime;
unsigned long messageEstimatedTime = 400;


String mouth  = "$$$F000111000111111111111111111000111000111111111111111111000111000000111000111111111111111111000111000111111111111111111000111000";
uint8_t mouthIndex = 0;
/*
char* mouthAnimations[5] = {"$$$F000010000111111111111111111000010000111111111111111111000010000000010000111111111111111111000010000111111111111111111000010000",
                            "$$$F000111000111111111111111111000111000111111111111111111000111000000111000111111111111111111000111000111111111111111111000111000",
                            "$$$F001111100111111111111111111001111100111111111111111111001111100001111100111111111111111111001111100111111111111111111001111100",
                            "$$$F000111000111111111111111111000111000111111111111111111000111000000111000111111111111111111000111000111111111111111111000111000",
                          "$$$F000010000111111111111111111000010000111111111111111111000010000000010000111111111111111111000010000111111111111111111000010000"};*/
                          

static const uint8_t PROGMEM // Bitmaps are stored in program memory
  blinkImg[][8] = {    // Eye animation frames
  { B00111100,         // Fully open eye
    B01111110,
    B11111111,
    B11111111,
    B11111111,
    B11111111,
    B01111110,
    B00111100 },
  { B00000000,
    B01111110,
    B11111111,
    B11111111,
    B11111111,
    B11111111,
    B01111110,
    B00111100 },
  { B00000000,
    B00000000,
    B00111100,
    B11111111,
    B11111111,
    B11111111,
    B00111100,
    B00000000 },
  { B00000000,
    B00000000,
    B00000000,
    B00111100,
    B11111111,
    B01111110,
    B00011000,
    B00000000 },
  { B00000000,         // Fully closed eye
    B00000000,
    B00000000,
    B00000000,
    B10000001,
    B01111110,
    B00000000,
    B00000000 } };


uint16_t j = (384 * 5) - 1 ;

uint8_t  blinkIndex[] = { 1, 2, 3, 4, 3, 2, 1 }; // Blink bitmap sequence
uint8_t  blinkCountdown = 100; // Countdown to next blink (in frames)
uint8_t  gazeCountdown  =  75; // Countdown to next eye movement
uint8_t  gazeFrames     =  50; // Duration of eye movement (smaller = faster)

int8_t  eyeX = 3;
int8_t eyeY = 3;   // Current eye position
int8_t  newX = 3;
int8_t newY = 3;   // Next eye position
int8_t  dX   = 0;
int8_t dY   = 0;   // Distance from prior to new position

byte incomingByte;

static const byte MOTOR_COMMAND_DELIMITER = 123;
static const byte MESSAGE_DELIMITER = 122;
static const byte PIN_DELIMITER = 121;

static const byte MOTOR_FORWARD = 70;
static const byte MOTOR_RELEASE = 82;
static const byte MOTOR_BACKWARD = 66;
	
// bytes indicating servo movement.
static const byte SERVO_UP = 1;
static const byte SERVO_UP_RIGHT = 2;
static const byte SERVO_RIGHT = 3;
static const byte SERVO_DOWN_RIGHT = 4;
static const byte SERVO_DOWN = 5;
static const byte SERVO_DOWN_LEFT = 6;
static const byte SERVO_LEFT = 7;
static byte SERVO_UP_LEFT = 8;
static const byte SERVO_NOTHING = 9;

boolean isTransmittingMessage = false;

byte motorCommand[5];
int motorCmdIndex = 0;
long previousMillis = 0; 
String currentState;



void setup() {
  motorShield.begin();
  eyematrix.begin(matrixAddr);
  currentState = "nothing";
  mouthSerial.begin(9600);
  mouthSerial.println("$$$SPEED100");
  Serial.begin(115200);
  // Seed random number generator from an unused analog input:
  randomSeed(analogRead(A0));
  // Initialize each matrix object:
  eyematrix.setRotation(3);
  mouthSerial.println(mouth);
  
  bottomServo.attach(10);
  topServo.attach(9);  // attaches the servo on pin 9 to the servo object 
  bottomServo.write(bottomPos);
  topServo.write(topPos);
  
}

void loop() {
  handleSerialInput();

  servoMovementTimer = mouthTimer = eyeTimer = messageCurrentTime = millis();
  
  eyeAcc += eyeTimer - eyeLastTimer;
  mouthAcc += mouthTimer - mouthLastTimer;
  servoMovementTimerAcc += servoMovementTimer - servoMovementLastTimer;
  
  
  mouthLastTimer = mouthTimer;
  eyeLastTimer = eyeTimer;
  servoMovementLastTimer = servoMovementTimer;
  
/*  while (mouthAcc >= 800)
  {
    animateMouth();
   mouthAcc = 0; 
  }
  */
  while (eyeAcc >= 70)
  {
    animateEyes();
    eyeAcc = 0;
  } 
  
  if (isTransmittingMessage)
  {
    if (messageCurrentTime - messageStartTime >= messageEstimatedTime)
    {
         mouthSerial.println(mouth);
         //mouthSerial.println("done");
        isTransmittingMessage = false;
    }
  }
}

void animateEyes() {
   // Draw eyeball in current state of blinkyness (no pupil).  Note that
  // only one eye needs to be drawn.  Because the two eye matrices share
  // the same address, the same data will be received by both.
  eyematrix.clear();
  // When counting down to the next blink, show the eye in the fully-
  // open state.  On the last few counts (during the blink), look up
  // the corresponding bitmap index.
  eyematrix.drawBitmap(0, 0,
    blinkImg[
      (blinkCountdown < sizeof(blinkIndex)) ? // Currently blinking?
      blinkIndex[blinkCountdown] :            // Yes, look up bitmap #
      0                                       // No, show bitmap 0
    ], 8, 8, LED_ON);
  // Decrement blink counter.  At end, set random time for next blink.
 if(--blinkCountdown == 0) blinkCountdown = random(5, 180);

  // Add a pupil (2x2 black square) atop the blinky eyeball bitmap.
  // Periodically, the pupil moves to a new position...
  
  if(--gazeCountdown <= gazeFrames) {
    // Eyes are in motion - draw pupil at interim position
    eyematrix.fillRect(
      newX - (dX * gazeCountdown / gazeFrames),
      newY - (dY * gazeCountdown / gazeFrames),
      2, 2, LED_OFF);
    if(gazeCountdown == 0) {    // Last frame?
      eyeX = newX; eyeY = newY; // Yes.  What's new is old, then...
      do { // Pick random positions until one is within the eye circle
        newX = random(7); newY = random(7);
        dX   = newX - 3;  dY   = newY - 3;
      } while((dX * dX + dY * dY) >= 10);      // Thank you Pythagoras
      dX            = newX - eyeX;             // Horizontal distance to move
      dY            = newY - eyeY;             // Vertical distance to move
      gazeFrames    = random(3, 15);           // Duration of eye movement
      gazeCountdown = random(gazeFrames, 120); // Count to end of next movement
    }
  } else {
    // Not in motion yet -- draw pupil at current static position
    eyematrix.fillRect(eyeX, eyeY, 2, 2, LED_OFF);
  }

  eyematrix.writeDisplay();
}

void animateMouth() {

  mouthIndex++;
  if (mouthIndex > 4)
  {
   mouthIndex = 0; 
  }
  mouthSerial.println(mouth);
}

void flushSerial()
  {
   while(Serial.available())
        Serial.read(); 
  }
  
void handleSerialInput()
{
    if (Serial.available() > 0)
  {
    incomingByte = Serial.read();
    if (incomingByte == MOTOR_COMMAND_DELIMITER && currentState == "nothing")
    {
     currentState = "motor";
     return;
    }
    else if (incomingByte == MESSAGE_DELIMITER && currentState == "nothing")
    {
     currentState = "message";
     return;
    }
    else if (incomingByte == PIN_DELIMITER && currentState == "nothing")
    {
     currentState = "pin";
     return;
    }
    
    
    if (currentState == "motor")
      {
        handleRobotMovement(incomingByte);
      }
      
      else if (currentState == "message")
      {
        handleRobotIncomingMessage(incomingByte);
      }
    }
}

void handleRobotMovement(byte incomingByte)
  {
      if (motorCmdIndex <= 4)
      {
        motorCommand[motorCmdIndex] = incomingByte;
        motorCmdIndex++;
      }
      
     else if (motorCmdIndex >= 5 && currentState == "motor")
     {
       byte leftDirection = motorCommand[0];
       byte leftSpeed= motorCommand[1] * 2;
       
       byte rightDirection = motorCommand[2];
       byte rightSpeed = motorCommand[3] * 2;
       runMotor(leftSpeed, leftDirection, leftMotor);

       runMotor(rightSpeed, rightDirection, rightMotor);
       
       while(servoMovementTimerAcc >= 10)
       {
         byte servoMovement = motorCommand[4];
         
         if (servoMovement != SERVO_NOTHING)
         {
           moveServo(servoMovement);
         }
         servoMovementTimerAcc = 0;
       }
       motorCmdIndex = 0;
       currentState = "nothing";
     }
  }

void handleRobotIncomingMessage(byte incomingByte)
  {
    
    if ((char)incomingByte != '\n' && robotMessageIndex < 38)
    {
      robotMessage[robotMessageIndex] = (char)incomingByte;
      robotMessageIndex++;
      
    }
    else {
      robotMessage[robotMessageIndex + 1] = '\0';
      String messageAsString(robotMessage);
      mouthSerial.println("$$$ALL,OFF");
      mouthSerial.println(messageAsString);    
      messageStartTime = millis();
      messageEstimatedTime = messageAsString.length() * 700;
      isTransmittingMessage = true;
      currentState = "nothing";
      robotMessageIndex = 0;
      memset(&robotMessage[0], 0, sizeof(robotMessage));
    }
  }

void runMotor(byte motorSpeed, byte motorDirection, Adafruit_DCMotor *motor)
  {
   motor->setSpeed(motorSpeed); 
   
   switch(motorDirection) {
    case MOTOR_FORWARD:
       motor->run(FORWARD);
       break;
    case MOTOR_BACKWARD:
       motor->run(BACKWARD);
       break;
    case MOTOR_RELEASE:
       motor->run(RELEASE);   
       break;
   }
  }
  
  
void moveServo(byte servoDirection)
  {
    
  if (servoDirection == SERVO_DOWN && topPos < 150)
  {
   topPos = topPos + 5; 
  }
  
  else if (servoDirection == SERVO_UP && topPos >= 110)
  {
    topPos = topPos - 5; 
  }

  else if (servoDirection == SERVO_LEFT && bottomPos < 160)
  {
   bottomPos = bottomPos + 5; 
  }
  
  else if (servoDirection == SERVO_RIGHT && bottomPos  >= 20)
  {
    bottomPos = bottomPos - 5; 
  }  
  
  
    else if (servoDirection == SERVO_DOWN_RIGHT && bottomPos  >= 20 && topPos < 150)
  {
    topPos = topPos + 5; 
    bottomPos = bottomPos - 5; 
  }  
  
    else if (servoDirection == SERVO_DOWN_LEFT && bottomPos < 160 && topPos < 150)
  {
       topPos = topPos + 5; 
       bottomPos = bottomPos + 5; 
  }  
  
    else if (servoDirection == SERVO_UP_RIGHT && topPos >= 110 && bottomPos  >= 20)
  {
    bottomPos = bottomPos - 5;
    topPos = topPos - 5; 
  }  
  
    else if (servoDirection == SERVO_UP_LEFT && topPos >= 110 && bottomPos < 160)
  {
    bottomPos = bottomPos + 5; 
    topPos = topPos - 5; 
  }  
  
  bottomServo.write(bottomPos);
  topServo.write(topPos);  // 
  
  }
