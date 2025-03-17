#if defined(WIFI_SERVER) || defined(WIFI_CLIENT)
#ifndef RELAY_WIFI_H // There's a name conflict with `WIFI_H` in WiFi101.h
#define RELAY_WIFI_H

#include <Arduino.h>
#include <WiFi101.h>
#include "constants.h"

int networkStatus = WL_IDLE_STATUS;

void wifiSetup();
bool connectToNetwork(IPAddress *ip, IPAddress *dns, IPAddress *gateway, IPAddress *subnet);
void flashLed();

#endif
#endif
