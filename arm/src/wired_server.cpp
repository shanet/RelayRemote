#ifdef WIRED_SERVER

#include "server.h"

void setup() {
  setupRelayPins();

  // Start the server
  Ethernet.begin(mac, ip, gateway, subnet);
  server.begin();
}

void loop() {
  // Get a client from the server
  EthernetClient client = server.available();

  if(!client) {
    return;
  }

  while(client.connected()) {
    if(client.available()) {
      processMessage(client);

      // We're done with this client. Disconnect it.
      client.stop();
    }
  }
}

#endif
