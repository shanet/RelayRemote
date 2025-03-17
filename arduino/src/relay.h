#ifndef RELAY_H
#define RELAY_H

#include <Arduino.h>
#include <limits.h>
#include "constants.h"

const char PINS[] = {CONFIGURED_PINS};

char pinStates[MAX_PIN + 1];

char getPinState(char pin);
void setPinState(char pin, char state);
void setupRelayPins();
void togglePinState(char pin);
bool isConfiguredPin(char pin);

#endif
