#include <PubSubClient.h>
#include <WiFi.h>
#include <WiFiClient.h>
#include <WiFiServer.h>
#include <WiFiUdp.h>
#include <WiFiClientSecure.h>

int pushButton1 = 15;
int pushButton2 = 5;
int ledPin1 = 23;
int ledPin2 = 4;

int buttonState1 = 0;
int buttonState2 = 0;
int lastState1 = 0;
int lastState2 = 0;
int ledState1 = 0;
int ledState2 = 0;

bool door1 = false;
bool door2 = false;

String alarmFixer = "";
String forNotification = "";

bool pendingFix = false;
int counter = 0;

const char* ssid = "xxx"; 
const char* password = "xxx"; 

const char* mqttServer = "8a33a3616838419591e76673e87f395d.s1.eu.hivemq.cloud"; 
const int mqttPort = 8883; 
const char* mqttUser = "esp32";
const char* mqttPassword = "Super@admin!1"; 

WiFiClientSecure wifiClient;
PubSubClient mqttClient(wifiClient); 

void setup() {
  Serial.begin(115200);  

  // Connect to Wi-Fi 
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) { 
    delay(1000); 
  } 
  Serial.println("Connected");
  
  wifiClient.setInsecure();

  // Set up MQTT client 
  mqttClient.setServer(mqttServer, mqttPort); 
  mqttClient.setCallback(callback);
  mqttClient.setKeepAlive(60);

  pinMode(pushButton1, INPUT);
  pinMode(ledPin1, OUTPUT);

  pinMode(pushButton2, INPUT);
  pinMode(ledPin2, OUTPUT);

  while (!mqttClient.connected()) {
      if (mqttClient.connect("esp", mqttUser, mqttPassword)) {
          Serial.println("mqtt broker connected");
      } else {
          Serial.print("failed with state ");
          Serial.print(mqttClient.state());
          delay(2000);
      }
  }

  mqttClient.subscribe("topic/androidPhone");
}

void loop() {

  if (forNotification == "fixed"){
    mqttClient.publish("topic/alarm", "alarm defused");
    forNotification = "";
  }

  if (!ledState1 && !ledState2) {

    alarmFixer = "";

    ledState1 = 0;
    ledState2 = 0;
    counter = 0;
    digitalWrite(ledPin2, 0);
    digitalWrite(ledPin1, 0);
    //Serial.println("defused");
  }

  lastState1 = buttonState1;
  buttonState1 = digitalRead(pushButton1);

  lastState2 = buttonState2;
  buttonState2 = digitalRead(pushButton2);

  if (lastState1 == 1 && buttonState1 == 0) {
    ledState1 = !ledState1;

    if (!ledState2) {
      digitalWrite(ledPin2, ledState1);
    }

    if (ledState1 == 1){
      if (ledState2 == 1 && ledState1 == 1){
        mqttClient.publish("topic/alarm", "door1door2");
      }
      else{
        mqttClient.publish("topic/alarm", "door1");
      }
    }

    if (ledState1 == 0){
      if (ledState2 == 0){
        mqttClient.publish("topic/alarm", "door1closed");
      }
      else{
        mqttClient.publish("topic/alarm", "door2");
      }
    }
    
    //Serial.println(ledState1);

    if (alarmFixer == "" && ledState1){
      pendingFix = true;
    }
  }

  if (lastState2 == 1 && buttonState2 == 0) {
    ledState2 = !ledState2;

    if (!ledState1) {
      digitalWrite(ledPin2, ledState2);
    }
      
    if (ledState2 == 1) {
      if (ledState2 == 1 && ledState1 == 1) {
        mqttClient.publish("topic/alarm", "door1door2");
      }
      else{
        mqttClient.publish("topic/alarm", "door2");
      }
    }

    if (ledState2 == 0) {
      if (ledState1 == 0) {
        mqttClient.publish("topic/alarm", "door2closed");
      }
      else{
        mqttClient.publish("topic/alarm", "door1");
      }
    }

    //Serial.println(ledState2);

    if (alarmFixer == "" && ledState2) {
      pendingFix = true;
    }
  }

  if (pendingFix == true) {

    if (alarmFixer == "fixed") {
      pendingFix = false;
      counter = 0;
    }

    //Serial.println("counting");
    counter++;

    // counter 1000 = 10 sec
    if (counter == 1000) {

      pendingFix = false;
      counter = 0;
    
      
      if (ledState1 == 1 && ledState2 == 1) {
        mqttClient.publish("topic/alarm", "alarm12");
      }
      else {
        if (ledState1 == 1) {
          mqttClient.publish("topic/alarm", "alarm1");
        }

        if (ledState2 == 1) {
          mqttClient.publish("topic/alarm", "alarm2");
        }
      }

      digitalWrite(ledPin1, 1);
    }
  }

  mqttClient.loop();

  delay(10); // Add a small delay to prevent flooding the MQTT broker with messages
}


void callback(char* topic, byte* payload, unsigned int length) { 
  // Handle incoming message 
  String message = ""; 

  for (int i = 0; i < length; i++) { 
    message += (char)payload[i]; 
  } 

  Serial.println("Message received on topic " + String(topic) + ": " + message); 
  alarmFixer = message;
  forNotification = message;
} 