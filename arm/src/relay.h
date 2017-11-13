#ifndef RELAY_H
#define RELAY_H

#include <SPI.h>
#include "constants.h"

#ifdef WIRED_SERVER
  #include <Ethernet.h>
#endif

#if defined(WIFI_SERVER) || defined(WIFI_CLIENT)
  #include <WiFi101.h>
#endif

int getPins(Client &client);
void pinHigh(int pin);
void pinLow(int pin);
int setPin(Client &client);
void setupRelayPins();

extern void abortClient(Client &client);

char pinStates[MAX_RELAY_PIN + 1];

#endif
