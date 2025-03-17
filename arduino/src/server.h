#ifndef SERVER_H
#define SERVER_H

#include "constants.h"
#include "http_responses.h"

#ifdef WIFI_SERVER
  #include <WiFi101.h>

  IPAddress ip(NETWORK_IP);
  IPAddress dns(NETWORK_GATEWAY);
  IPAddress gateway(NETWORK_GATEWAY);
  IPAddress subnet(NETWORK_SUBNET);

  WiFiServer server(HTTP_PORT);
#endif

#ifdef WIRED_SERVER
  #include <Ethernet.h>
  #include <SPI.h>

  byte ip[]      = {NETWORK_IP};
  byte mac[]     = {NETWORK_MAC};
  byte gateway[] = {NETWORK_GATEWAY};
  byte subnet[]  = {NETWORK_SUBNET};

  EthernetServer server = EthernetServer(HTTP_PORT);
#endif

extern void setupRelayPins();
extern void wifiSetup();
extern bool connectToNetwork(IPAddress *ip, IPAddress *dns, IPAddress *gateway, IPAddress *subnet);
extern void flashLed();

#endif
