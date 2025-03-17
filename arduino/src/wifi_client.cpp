#ifdef WIFI_CLIENT
#include "client.h"

void setup() {
  Serial.begin(SERIAL_BAUD);
  wifiSetup();

  pinMode(BUTTON_PIN, INPUT);
  pinMode(LED_PIN, OUTPUT);

  digitalWrite(LED_PIN, HIGH);
}

void loop() {
  // Ensure we're still connected to the network
  connectToNetwork(&ip, &dns, &gateway, &subnet);

  // When the button is pressed toggle the relays and flash the LED
  if(digitalRead(BUTTON_PIN) == HIGH) {
    Serial.println("Button press registered");
    flashLed();

    toggleRelays();

    // Sleep for 3 second to prevent rapid toggling of relays
    delay(3000);
  }
}

void toggleRelays() {
  // Toggle each relay in the relays list with the set command
  size_t numRelays = sizeof(RELAY_CLIENTS_PINS) / sizeof(int);

  for(size_t i=0; i<numRelays; i++) {
    WiFiClient client;
    IPAddress ip;
    ip.fromString(RELAY_CLIENTS_IPS[i]);

    Serial.println("Sending request to " + RELAY_CLIENTS_IPS[i] + "/api/pins/" + String(RELAY_CLIENTS_PINS[i]));
    sendToggleRequest(client, ip, HTTP_PORT, "POST", "/api/pins/" + String(RELAY_CLIENTS_PINS[i]), "state=toggle");

    // There is apparently an issue with rapidly opening multiple connections so sleep for a bit between relays
    delay(100);
  }
}

void sendToggleRequest(WiFiClient &client, IPAddress ip, int port, String method, String route, String body="") {
  char failureCount = 0;

  // Try to send the request a few times to handle flaky networks
  while(true) {
    if(client.connect(ip, port)) break;

    failureCount++;

    if(failureCount >= 3) {
      Serial.println("Failed to connect to server, aborting");
      return;
    }

    Serial.println("Failed to connect to server, retrying");
  }

  failureCount = 0;

  while(true) {
    String responseHeaders = sendRequest(client, method, route, body);
    if(responseHeaders.indexOf("303 See Other") >= 0) break;

    failureCount++;

    if(failureCount >= 3) {
      Serial.println("Relay toggle request failed, aborting");
      return;
    }

    Serial.println("Relay toggle request failed, retrying");
  }

  client.stop();
  Serial.println("Request sent");
}

#endif
