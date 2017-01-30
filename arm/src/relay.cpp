#include "relay.h"

void setup() {
  WiFi.setPins(8, 7, 4, 2);

  pinMode(BUTTON_PIN, INPUT);
  pinMode(LED_PIN, OUTPUT);

  digitalWrite(LED_PIN, HIGH);
  connectToNetwork();
}

void loop() {
  // When the button is pressed toggle the relays and flash the LED
  if(digitalRead(BUTTON_PIN) == HIGH) {
    flashLed();
    toggleRelays();

    // Sleep for 3 second to prevent rapid toggling of relays
    delay(3000);
  }
}

void connectToNetwork() {
  while(networkStatus != WL_CONNECTED) {
    networkStatus = WiFi.begin(SSID, PASSPHRASE);

    // Flash the LED twice when connected to the network
    flashLed();
    flashLed();
  }
}

void toggleRelays() {
  // Connect to the network if not already connected
  connectToNetwork();

  // Toggle each relay in the relays list with the set command
  size_t numRelays = sizeof(relays) / sizeof(IPAddress);

  for(unsigned int i=0; i<numRelays; i++) {
    // There is apparently an issue with rapidly opening multiple connections so sleep for a bit between relays
    toggleRelay(relays[i]);
    delay(100);
  }
}

void toggleRelay(IPAddress ip) {
  WiFiClient client;

  if(client.connect(ip, PORT)) {
    client.print(COMMAND);
    client.stop();
  }
}

void flashLed() {
  digitalWrite(LED_PIN, LOW);
  delay(500);
  digitalWrite(LED_PIN, HIGH);
}
