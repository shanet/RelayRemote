#ifndef SERVER_H
#define SERVER_H

#include <SPI.h>
#include "constants.h"
#include "secrets.h"

// Edit these settings for WiFi server only
#ifdef WIFI_SERVER
  #include <WiFi101.h>

  // IMPORTANT: The network settings MUST BE CHANGED to something unique for each server
  IPAddress ip(10, 10, 12, 33);
  IPAddress dns(10, 10, 12, 1);
  IPAddress gateway(10, 10, 12, 1);
  IPAddress subnet(255, 255, 255, 0);

  WiFiServer server(PORT);
#endif

// Edit these settings for wired server only
#ifdef WIRED_SERVER
  #include <Ethernet.h>

  // IMPORTANT: The IP AND MAC MUST BE CHANGED to something unique for each server
  // The gateway will probably need changed as well
  byte ip[]      = {10, 10, 10, 30};
  byte mac[]     = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xEE};
  byte gateway[] = {10, 10, 10, 1};
  byte subnet[]  = {255, 255, 255, 0};

  EthernetServer server = EthernetServer(PORT);
#endif

extern void processMessage(Client &client);
extern void setupRelayPins();
extern void wifiSetup();
extern void connectToNetwork(IPAddress *ip, IPAddress *dns, IPAddress *gateway, IPAddress *subnet);
extern void flashLed();

#endif
