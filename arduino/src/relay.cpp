#if defined(WIRED_SERVER) || defined(WIFI_SERVER)
#include "relay.h"

void setupRelayPins() {
  // Set the relay pins as output
  for(char index=0; index<(sizeof(PINS)/sizeof(PINS[0])); index++) {
    pinMode(PINS[index], OUTPUT);
  }

  pinMode(LED_PIN, OUTPUT);
}

void setPinState(char pin, char state) {
  if(!isConfiguredPin(pin)) return;

  digitalWrite(pin, state);
  pinStates[pin] = state;
}

void togglePinState(char pin) {
  setPinState(pin, (pinStates[pin] == HIGH ? LOW : HIGH));
}

char getPinState(char pin) {
  if(!isConfiguredPin(pin)) return 0;

  return pinStates[pin] == HIGH;
}

bool isConfiguredPin(char pin) {
  for(char index=0; index<(sizeof(PINS)/sizeof(PINS[0])); index++) {
    if(PINS[index] == pin) return true;
  }

  return false;
}

#endif
