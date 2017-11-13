#ifndef NETWORK_H
#define NETWORK_H

#ifdef WIRED_SERVER
  #include <Ethernet.h>
#endif

#if defined(WIFI_SERVER) || defined(WIFI_CLIENT)
  #include <WiFi101.h>
  int networkStatus = WL_IDLE_STATUS;
#endif

#include "constants.h"
#include "secrets.h"

void wifiSetup();
void connectToNetwork(IPAddress *ip, IPAddress *dns, IPAddress *gateway, IPAddress *subnet);
void processMessage(Client &client);
void abortClient(Client &client);
void flashLed();

extern int getPins(Client &client);
extern int setPin(Client &client);

#endif
