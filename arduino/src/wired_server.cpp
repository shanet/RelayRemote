#ifdef WIRED_SERVER

#include "server.h"

void setup() {
  Serial.begin(SERIAL_BAUD);
  setupRelayPins();

  Ethernet.begin(mac, ip, gateway, subnet);
  server.begin();
}

void loop() {
  Serial.println("Listening...");

  EthernetClient client = server.available();

  if(client && client.connected()) {
    Serial.println("Client connected!");
    handleRequest(client);
  }

  delay(100);
}

#endif
