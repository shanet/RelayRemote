#ifdef WIFI_SERVER

#include "server.h"

void setup() {
  Serial.begin(SERIAL_BAUD);

  setupRelayPins();
  wifiSetup();

  connectToNetwork(&ip, &dns, &gateway, &subnet);
  server.begin();
}

void loop() {
  Serial.println("Listening...");

  // Ensure we're still connected to the network
  if(connectToNetwork(&ip, &dns, &gateway, &subnet)) server.begin();

  WiFiClient client = server.available();

  if(client && client.connected()) {
    Serial.println("Client connected!");
    handleRequest(client);
    flashLed();
  }

  delay(100);
}

#endif
