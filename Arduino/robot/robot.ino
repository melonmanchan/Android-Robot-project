
#include <Arduino.h>
#include <Wire.h>
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include "SPI.h" // Comment out this line if using Trinket or Gemma
#include <SoftwareSerial.h>
#include <Adafruit_MotorShield.h>
#include <Servo2.h>

#define  c     3830    // 261 Hz
#define  d     3400    // 294 Hz
#define  e     3038    // 329 Hz
#define  f     2864    // 349 Hz
#define  g     2550    // 392 Hz
#define  a     2272    // 440 Hz
#define  b     2028    // 493 Hz
#define  C     1912    // 523 Hz 

int melody[] = {c, d, e, f, g, a, b, C};

Adafruit_MotorShield motorShield = Adafruit_MotorShield();
Adafruit_DCMotor *leftMotor = motorShield.getMotor(3);
Adafruit_DCMotor *rightMotor = motorShield.getMotor(4);

Adafruit_8x8matrix eyematrix = Adafruit_8x8matrix();

// Bit banged serial for the mouth
SoftwareSerial mouthSerial(2, 3); // RX, TX

// char-array to hold the incoming message
char robotMessage[40];
byte robotMessageIndex = 0;

// Two robot servos
Servo bottomServo;
Servo topServo; 
 
// Starting positions for servos.
byte bottomPos = 90;
byte topPos = 150;


static const uint8_t matrixAddr = 0x71;

// Timers used to "simulate" threading
unsigned long eyeTimer;
unsigned long eyeLastTimer;
unsigned long eyeAcc;

unsigned long messageStartTime;
unsigned long messageCurrentTime;
unsigned long messageEstimatedTime = 400;

unsigned long timeSinceLastCmd = 0;
unsigned long currentCmdTime;

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

// Bytes to know what messages are about to be received
static const byte MOTOR_COMMAND_DELIMITER = 123;
static const byte MESSAGE_DELIMITER = 122;
static const byte PIN_TOGGLE_DELIMITER = 121;
static const byte PIN_PWM_DELIMITER = 120;

// Bytes indicating direction of motor
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
byte motorCmdIndex = 0;
long previousMillis = 0; 

typedef enum {NOTHING, MOTOR, MESSAGE, TOGGLE, PINPWM} state;
state currentState;

void setup() {
  motorShield.begin();
  eyematrix.begin(matrixAddr);
  currentState = NOTHING;
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

  currentCmdTime = eyeTimer = messageCurrentTime = millis();
  
  eyeAcc += eyeTimer - eyeLastTimer;
  eyeLastTimer = eyeTimer;
  
  while (eyeAcc >= 70)
  {
    animateEyes();
    eyeAcc = 0;
  } 
  
  if (isTransmittingMessage)
  {
    
    if (messageCurrentTime % 125 == 0)
    {
     tone(5,  melody[random(0, sizeof(melody)-1)]);
    }
    
    if (messageCurrentTime - messageStartTime >= messageEstimatedTime)
    {
         noTone(5);
         mouthSerial.println(mouth);
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
  
  if(--gazeCountdown <= gazeFrames)
  {
    // Eyes are in motion - draw pupil at interim position
    eyematrix.fillRect(
      newX - (dX * gazeCountdown / gazeFrames),
      newY - (dY * gazeCountdown / gazeFrames),
      2, 2, LED_OFF);
    if(gazeCountdown == 0)
    {    // Last frame?
      eyeX = newX; eyeY = newY; // Yes.  What's new is old, then...
      do
      { // Pick random positions until one is within the eye circle
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


void flushSerial()
  {
   while(Serial.available())
        Serial.read(); 
  }
  
void handleSerialInput()
  {
    if (Serial.available() > 0)
    {
      timeSinceLastCmd = millis();
      incomingByte = Serial.read();
      
      if (incomingByte == MOTOR_COMMAND_DELIMITER && currentState == NOTHING)
      {
       currentState = MOTOR;
       return;
      }
      
      else if (incomingByte == MESSAGE_DELIMITER && currentState == NOTHING)
      {
       currentState = MESSAGE;
       return;
      }
      
      else if (incomingByte == PIN_TOGGLE_DELIMITER && currentState == NOTHING)
      {
       currentState = TOGGLE;
       return;
      }
      
      else if (incomingByte == PIN_PWM_DELIMITER && currentState == NOTHING)
      {
       currentState = PINPWM;
       return; 
      }
      
      if (currentState == MOTOR)
       {
         handleRobotMovement(incomingByte);
       }
        
       else if (currentState == MESSAGE)
       {
         handleRobotIncomingMessage(incomingByte);
       }
       
       else if (currentState == TOGGLE)
       {
         delay(25);
         
         byte secondByte = Serial.read();
         
         changePinToggleState(incomingByte, secondByte);
       }
        
       else if (currentState == PINPWM)
       {
        delay(25);
        // multiply by two since Java bytes are from -127 to 127 and we want the value from 0 to 255.
        byte secondByte = Serial.read() * 2;
         
         // since the maximum value of 127 * 2 is 254, this way we can handle full voltage pwm
         if (secondByte == 254)
           secondByte = 255;
           
        changePinPWMState(incomingByte, secondByte);
       }
      }
      
      else
      {
       if (currentCmdTime - timeSinceLastCmd > 10000)
       {
         currentState = NOTHING;
         runMotor(0, MOTOR_RELEASE, leftMotor);
         runMotor(0, MOTOR_RELEASE, rightMotor);
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
      
     else if (motorCmdIndex >= 5 && currentState == MOTOR)
     {
       byte leftDirection = motorCommand[0];
       byte leftSpeed= motorCommand[1] * 2;
       
       byte rightDirection = motorCommand[2];
       byte rightSpeed = motorCommand[3] * 2;
       runMotor(leftSpeed, leftDirection, leftMotor);

       runMotor(rightSpeed, rightDirection, rightMotor);
       
       byte servoMovement = motorCommand[4];
         
       if (servoMovement != SERVO_NOTHING)
       {
         moveServo(servoMovement);
        }
       motorCmdIndex = 0;
       currentState = NOTHING;
     }
  }

void handleRobotIncomingMessage(byte incomingByte)
  { 
    if ((char)incomingByte != '\n' && robotMessageIndex < 38)
    {
      robotMessage[robotMessageIndex] = (char)incomingByte;
      robotMessageIndex++;
      
    }
    else 
    {
      // null-terminated string
      robotMessage[robotMessageIndex + 1] = '\0';
      String messageAsString(robotMessage);
      mouthSerial.println("$$$ALL,OFF");
      mouthSerial.println(messageAsString);    
      messageStartTime = millis();
      messageEstimatedTime = messageAsString.length() * 600;
      isTransmittingMessage = true;
      currentState = NOTHING;
      robotMessageIndex = 0;
      // most efficient way to reallocate a char array to null.
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
   topPos = topPos + 10; 
  }
  
  else if (servoDirection == SERVO_UP && topPos >= 110)
  {
    topPos = topPos - 10; 
  }

  else if (servoDirection == SERVO_LEFT && bottomPos < 160)
  {
   bottomPos = bottomPos + 10; 
  }
  
  else if (servoDirection == SERVO_RIGHT && bottomPos  >= 20)
  {
    bottomPos = bottomPos - 10; 
  }  
  
  else if (servoDirection == SERVO_DOWN_RIGHT && bottomPos  >= 20 && topPos < 150)
  {
    topPos = topPos + 10; 
    bottomPos = bottomPos - 10; 
  }  
  
  else if (servoDirection == SERVO_DOWN_LEFT && bottomPos < 160 && topPos < 150)
  {
       topPos = topPos + 10; 
       bottomPos = bottomPos + 10; 
  }  
  
  else if (servoDirection == SERVO_UP_RIGHT && topPos >= 110 && bottomPos  >= 20)
  {
    bottomPos = bottomPos - 10;
    topPos = topPos - 10; 
  }  
  
  else if (servoDirection == SERVO_UP_LEFT && topPos >= 110 && bottomPos < 160)
  {
    bottomPos = bottomPos + 10; 
    topPos = topPos - 10; 
  }  
  
  bottomServo.write(bottomPos);
  topServo.write(topPos);
  }
  
  void changePinToggleState(byte pin_num, byte isOn)
  { 
   int pin_value = LOW;
       
   if (isOn == 1) pin_value = HIGH;
   else if (isOn == 0) pin_value = LOW;
   
   switch(pin_num)
   {
     case 4:
       pinMode(4, OUTPUT);
       digitalWrite(4, isOn);
       break;
       
     case 6:
       pinMode(6, OUTPUT);
       digitalWrite(6, isOn);
       break;
       
     case 7:
       pinMode(7, OUTPUT);
       digitalWrite(7, isOn);     
       break;
       
     case 8:
       pinMode(8, OUTPUT);
       digitalWrite(8, isOn);     
       break;
       
     case 11:
       pinMode(11, OUTPUT);
       digitalWrite(11, isOn);     
       break;
       
     case 12:
       pinMode(12, OUTPUT);
       digitalWrite(12, isOn);     
       break;
       
     case 13:
       pinMode(13, OUTPUT);     
       digitalWrite(13, isOn);     
       break;     
   }
   currentState = NOTHING; 
  }
  
  void changePinPWMState(byte pin_num, byte value)
  {
   analogWrite(pin_num, value); 
   currentState = NOTHING;
  }
