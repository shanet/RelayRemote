#ifdef WIFI_CLIENT
#ifndef CLIENT_H
#define CLIENT_H

#include <WiFi101.h>
#include "constants.h"
#include "http_requests.h"

IPAddress ip(NETWORK_IP);
IPAddress dns(NETWORK_GATEWAY);
IPAddress gateway(NETWORK_GATEWAY);
IPAddress subnet(NETWORK_SUBNET);

String RELAY_CLIENTS_IPS[] = {NETWORK_CLIENT_IPS};
int RELAY_CLIENTS_PINS[] = {NETWORK_CLIENT_PINS};

void toggleRelays();
void sendToggleRequest(WiFiClient &client, IPAddress ip, int port, String method, String route, String body);

extern void wifiSetup();
extern bool connectToNetwork(IPAddress *ip, IPAddress *dns, IPAddress *gateway, IPAddress *subnet);
extern void flashLed();

#endif
#endif
