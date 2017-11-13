#ifdef WIFI_SERVER

#include "server.h"

void setup() {
  setupRelayPins();
  wifiSetup();

  connectToNetwork(&ip, &dns, &gateway, &subnet);
  server.begin();
}

void loop() {
  // Get a client from the server
  WiFiClient client = server.available();

  if(!client) {
    return;
  }

  while(client.connected()) {
    if(client.available()) {
      processMessage(client);
      flashLed();

      // We're done with this client. Disconnect it.
      client.stop();
    }
  }
}

#endif
