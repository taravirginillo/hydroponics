/* 
 *  Bluetooh Basic: LED ON OFF - Avishkar
 *  Coder - Mayoogh Girish
 *  Website - http://bit.do/Avishkar
 *  Download the App : 
 *  This program lets you to control a LED on pin 13 of arduino using a bluetooth module
 */
#include <SoftwareSerial.h>
#include "DHT.h"

SoftwareSerial BTserial(0,1); // RX | TX

#define LED_PIN 7
#define DHT_PIN 13
#define RAIN_PIN A0
#define INTAKE_FAN_PIN 3
#define RGB_LIGHT_RELAY_IN1 12
#define RGB_LIGHT_RELAY_IN2 11

#define DHT_TYPE DHT11
DHT dht(DHT_PIN, DHT_TYPE);

char Incoming_value = 0;                //Variable for storing Incoming_value
int dhtVal = 252;
int rainVal = 0;

float vpdValue = 0;
float desiredVPD = 0.5;

int sensorValue = 0; //value read from pot
int outputValue = 0; //value output to PWM (analog out)

unsigned long time;
unsigned int rpm;
String stringRPM;

/*
 * # Source: 
# http://www.just4growers.com/stream/temperature-humidity-and-c02/vapor-pressure-deficit-the-hidden-force-on-your-plants.aspx

def vpLeaf(t):
    return((610.7*10**(7.5*t/(237.3+t)))/1000)
def vpAir(t, h):
    return(((h/100)*((610.7*10**(7.5*t/(237.3+t)))/1000)))

t = 24
h = 45

print(vpLeaf(t-2) - vpAir(t,h))
 */

void setup() 
{
  BTserial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
 // Serial.begin(9600);
  
  pinMode(LED_PIN, OUTPUT);        //Sets digital pin 7 as output pin
  pinMode(RAIN_PIN, INPUT);
  pinMode(RGB_LIGHT_RELAY_IN1, OUTPUT);
  pinMode(INTAKE_FAN_PIN, OUTPUT);   // OCR2B sets duty cycle

  dht.begin();
  
  // Set up Fast PWM on Pin 3
  TCCR2A = 0x23;     // COM2B1, WGM21, WGM20 
  // Set prescaler  
  TCCR2B = 0x0A;   // WGM21, Prescaler = /8
  // Set TOP and initialize duty cycle to zero(0)
  OCR2A = 79;    // TOP DO NOT CHANGE, SETS PWM PULSE RATE
  OCR2B = 0;    // duty cycle for Pin 3 (0-79) generates 1 500nS pulse even when 0 :

  
}
void loop()
{
  rainVal = analogRead(RAIN_PIN);

  float h = dht.readHumidity();
  // Read temperature as Celsius (the default)
  float t = dht.readTemperature();

  // Check if any reads failed and exit early (to try again).
  if (!isnan(h) && !isnan(t)) {
    vpdValue = calculateVPD(t,h);
    
    updateClimateControls(vpdValue, desiredVPD);
  }

  Incoming_value = BTserial.read();      //Read the incoming data and store it into variable Incoming_value
  if(Incoming_value == '1'){            //Checks whether value of Incoming_value is equal to 1 (OFF)
    digitalWrite(RGB_LIGHT_RELAY_IN1, LOW);  //If value is 1 then LED turns OFF
    OCR2B = 255;
  } else if(Incoming_value == '0') {      //Checks whether value of Incoming_value is equal to 0
    digitalWrite(RGB_LIGHT_RELAY_IN1, HIGH);   //If value is 0 then LED turns ON
    OCR2B = 0;
  }else if(Incoming_value == 'r'){
    //dhtVal = analogRead(DHT_PIN);
    //BTserial.print("VPD Value : ");
   // rainVal = analogRead(RAIN_PIN);
   String output = "Temp : " + String(t) + "\n Humidity : " + String(h) + "\n VPD: " + String(vpdValue);
    BTserial.print(output);
  } else{
  }                        
}

float calculateVPD(float t, float h)
{

  float tLeaf = t - 2;
  float vpLeaf = ((610.7*pow(10.0,(7.5*tLeaf/(237.3+tLeaf))))/1000);
  float vpAir = (((h/100)*((610.7*pow(10.0,(7.5*t/(237.3+t))))/1000)));

  return(vpLeaf - vpAir);
}



void updateClimateControls(float actual, float desired)
{
  if(actual < desired){
      digitalWrite(RGB_LIGHT_RELAY_IN1, HIGH);  
      OCR2B = 255;
  }
}
