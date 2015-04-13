/*
  Multicolor Lamp (works with Amarino and the MultiColorLamp Android app)
  
  - based on the Amarino Multicolor Lamp tutorial
  - receives custom events from Amarino changing color accordingly
  
  author: Bonifaz Kaufmann - December 2009
*/
 
#include <MeetAndroid.h>

// declare MeetAndroid so that you can call functions with it
MeetAndroid meetAndroid;

#define D4 4
#define D3 3
#define D2 2

// we need 3 PWM pins to control the leds

void setup()  
{
  // use the baud rate your bluetooth module is configured to 
  // not all baud rates are working well, i.e. ATMEGA168 works best with 57600
  Serial.begin(57600); 
  
  // register callback functions, which will be called when an associated event occurs.
  meetAndroid.registerFunction(left, 'l');
  meetAndroid.registerFunction(right, 'r');  
  meetAndroid.registerFunction(straight, 'g');
  meetAndroid.registerFunction(goal, 't');

  // set all color leds as output pins
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  pinMode(4, OUTPUT);

  // just set all leds to high so that we see they are working well
  digitalWrite(2, LOW);
  digitalWrite(3, LOW);
  digitalWrite(4, LOW);

}

void loop()
{
  meetAndroid.receive(); // you need to keep this in your loop() to receive events
}

/*
 * Whenever the multicolor lamp app changes the red value
 * this function will be called
 */
void left(byte flag, byte numOfValues)
{
  if(digitalRead(2) != HIGH)
  {
    digitalWrite(2, HIGH);
    delay(5000);
  }
  digitalWrite(2, LOW);
}

/*
 * Whenever the multicolor lamp app changes the green value
 * this function will be called
 */
void right(byte flag, byte numOfValues)
{
  if(digitalRead(3) != HIGH)
  {
    digitalWrite(3, HIGH);
    delay(5000);
  }
  digitalWrite(3, LOW);

}

void straight(byte flag, byte numOfValues)
{
  if(digitalRead(4) != HIGH)
  {
    digitalWrite(4, HIGH);
    delay(5000);
  }
  digitalWrite(4, LOW);
}

void goal(byte flag, byte numOfValues)
{
  if(digitalRead(2) != HIGH && digitalRead(3) != HIGH && digitalRead(4) != HIGH)
  {
    digitalWrite(2, HIGH);
    digitalWrite(3,HIGH);
    digitalWrite(4,HIGH);
    delay(5000);
  }
  digitalWrite(2, LOW);
  digitalWrite(3, LOW);
  digitalWrite(4, LOW);
}


