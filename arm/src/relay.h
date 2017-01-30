#ifndef RELAY_H

#include <SPI.h>
#include <WiFi101.h>

#include "secrets.h"

#define BUTTON_PIN 10
#define LED_PIN 13
#define PORT 2424
#define COMMAND "s-9-t"

IPAddress relays[3] = {
  IPAddress(10,10,10,30),
  IPAddress(10,10,10,31),
  IPAddress(10,10,10,32),
};

int networkStatus = WL_IDLE_STATUS;

void connectToNetwork();
void toggleRelays();
void toggleRelay(IPAddress ip);
void flashLed();

#endif
