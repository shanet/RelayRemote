#ifdef WIFI_CLIENT
#ifndef CLIENT_H
#define CLIENT_H

#include <WiFi101.h>
#include "constants.h"

IPAddress relays[3] = {
  IPAddress(10,10,10,30),
  IPAddress(10,10,12,31),
  IPAddress(10,10,12,33),
};

void toggleRelays();
void toggleRelay(IPAddress ip);

extern void wifiSetup();
extern bool connectToNetwork(IPAddress *ip, IPAddress *dns, IPAddress *gateway, IPAddress *subnet);
extern void flashLed();

#endif
#endif
