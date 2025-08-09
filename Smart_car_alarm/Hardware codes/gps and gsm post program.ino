
#include <RH_ASK.h>
#include <SPI.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>
#include <HardwareSerial.h>
#include <Wire.h>
#include <MPU6050.h>
#include <HardwareSerial.h>
#include <HTTPClient.h>
#include <TinyGPS++.h>


// Pins
#define BUZZER_PIN 32
#define RF_RECEIVE_PIN 15
#define INTRUSION_PIN 13
#define PIR_PIN 23
#define MODEM_TX 17
#define MODEM_RX 16
#define gpsRx_Pin 5
#define gpsTx_Pin 4
#define uartBaud 9600
#define serialBaud 115200
#define testLed_pin 12

// BLE threshold 
#define RSSI_THRESHOLD -65

// SIM settings
const char *ownerPhoneNumber = "989039107290";

// System flags
bool doorLocked = true;
bool deviceInRange = false;
bool sirenActive = false;
bool smsSent = false;
bool personDetectedSMSsent = false;
bool callMade = false;

unsigned long lastSeen = 0;

// ------------------ Gps CONFIG ------------------
const char* apn = "apn?";  // APN  SIM card 
const char* apiKey = "api?";  // ThingSpeak Write API Key


unsigned long lastPostTime = 0;
const unsigned long postInterval = 19500; // 20 seconds

String httpData = ""; 



bool sendATCommand(const char *command, const char *expected, unsigned long timeout) {
  sim800.println(command);
  Serial.print(">> ");
  Serial.println(command);
  unsigned long t = millis();
  String response = "";
  while (millis() - t < timeout) {
    if (sim800.available()) response += (char)sim800.read();
    if (response.indexOf(expected) != -1) {
      Serial.print("<< ");
      Serial.println(response);
      return true;
    }
  }
  Serial.print("!! Timeout/Error. Response: ");
  Serial.println(response);
  return false;
}

bool sendSMS(const char *number, const char *message) {
  if (!sendATCommand("AT", "OK", 2000)) return false;
  if (!sendATCommand("ATE0", "OK", 2000)) return false;
  if (!sendATCommand("AT+CMGF=1", "OK", 2000)) return false;

  String cmd = String("AT+CMGS=\"") + number + "\"";
  if (!sendATCommand(cmd.c_str(), ">", 3000)) return false;

  sim800.print(message);
  sim800.write(26);  // CTRL+Z

  unsigned long t = millis();
  String response = "";
  while (millis() - t < 10000) {
    if (sim800.available()) response += (char)sim800.read();
    if (response.indexOf("+CMGS:") != -1 || response.indexOf("OK") != -1) {
      Serial.println("<< SMS sent!");
      return true;
    }
  }

  Serial.println("!! SMS failed.");
  return false;
}

bool makeCall(const char *number) {
  Serial.println("Dialing owner...");
  String cmd = String("ATD") + number + ";";
  return sendATCommand(cmd.c_str(), "OK", 10000);
}



void setup() {
  Serial.begin(serialBaud);


  


  // SIM800
  sim800.begin(uartBaud, SERIAL_8N1, MODEM_RX, MODEM_TX);
  delay(1000);

  gpsSerial.begin(uartBaud, SERIAL_8N1, gpsRx_Pin, gpsTx_Pin);
  Serial.print("GPS UART connection started!");

  pinMode(testLed_pin, OUTPUT);
  digitalWrite(testLed_pin, LOW);
  
  Serial.println("SIM800L ThingSpeak POST Test Starting...");


  Serial.println("System ready.");
}


void loop() {

  gpsModule();
  delay(50);
}


bool sendThingSpeakPOST(const String& data) {
  if (!sendATCommand("AT", "OK", 2000)) return false;
  if (!sendATCommand("AT+SAPBR=3,1,\"Contype\",\"GPRS\"", "OK", 2000)) return false;

  String apnCmd = String("AT+SAPBR=3,1,\"APN\",\"") + apn + "\"";
  if (!sendATCommand(apnCmd.c_str(), "OK", 2000)) return false;

  if (!sendATCommand("AT+SAPBR=1,1", "OK", 10000)) return false;
  if (!sendATCommand("AT+HTTPINIT", "OK", 2000)) return false;

  if (!sendATCommand("AT+HTTPPARA=\"URL\",\"http://api.thingspeak.com/update\"", "OK", 3000)) return false;
  if (!sendATCommand("AT+HTTPPARA=\"CONTENT\",\"application/x-www-form-urlencoded\"", "OK", 2000)) return false;

  String lenCmd = String("AT+HTTPDATA=") + data.length() + ",10000";
  if (!sendATCommand(lenCmd.c_str(), "DOWNLOAD", 3000)) return false;

  sim800.print(data);
  delay(1000);

  if (!sendATCommand("AT+HTTPACTION=1", "+HTTPACTION: 1,", 10000)) return false;

  sendATCommand("AT+HTTPTERM", "OK", 2000);

  Serial.println(">> ThingSpeak POST sent!");
  return true;
}

  String urlEncode(String str) {
    str.replace(" ", "%20");
    str.replace("/", "%2F");
    str.replace("|", "%7C");
    str.replace(":", "%3A");
    return str;
}

void gpsModule(){

    if ((millis()-lastPostTime >= postInterval)){
      
    unsigned long startTime = millis();
    while (millis()-startTime <= 100 ){
      while (gpsSerial.available()>0){
      gps.encode(gpsSerial.read());
      }
    }

     if (gps.location.isUpdated()){
        
        char latStr[15];
        float latittude = gps.location.lat();
        dtostrf(latittude,0,6,latStr);
        Serial.print("lattitude: ");
        Serial.println(latStr);

        char lngStr[15];
        float longtittude = gps.location.lng();
        dtostrf(longtittude,0,6,lngStr);
        Serial.print("longtittude: ");
        Serial.println(lngStr);

        char speedStr[10];
        float speed = gps.speed.kmph();
        dtostrf(speed,0,2,speedStr);
        Serial.print("Speed: ");
        Serial.println(String(speedStr) + " kmph");

        int numSat = gps.satellites.value();
        Serial.print("number of Satellites: ");
        Serial.println(numSat);

        String exact_time = String(gps.date.year())+ "/"+String(gps.date.month())+"/"+String(gps.date.day())+" | "+
        String(gps.time.hour())+":"+String(gps.time.minute())+":" +String(gps.time.second());
        Serial.print("Time & date in UTC: ");
        Serial.println(exact_time);

        char altStr[10];
        float altitude = gps.altitude.meters();
        dtostrf(altitude,0,2,altStr);
        Serial.print("Altittude: ");
        Serial.println(altStr);

        char hdopStr[10];
        float hdop = gps.hdop.value()/100;
        dtostrf(hdop,0,1,hdopStr);
        Serial.print("HDOP accuracy: ");
        Serial.println(hdopStr);

        Serial.println("--------------End of Data---------------");

        httpData= "api_key=" + String(apiKey) + "&field1=" + String(latStr) + 
        "&field2=" + String(lngStr) + "&field3=" + String(speedStr) + "&field4=" + String(altStr) 
        + "&field5=" + urlEncode(exact_time) + "&field7=" + String(numSat) + "&field8=" + String(hdopStr);      
        Serial.println("Posting to ThingSpeak...");
        bool ok = sendThingSpeakPOST(httpData);

        lastPostTime = millis();
      }
    }

      

}
