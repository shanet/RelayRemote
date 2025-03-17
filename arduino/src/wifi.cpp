#if defined(WIFI_SERVER) || defined(WIFI_CLIENT)
#include "wifi.h"

void wifiSetup() {
  WiFi.setPins(8, 7, 4, 2);
}

bool connectToNetwork(IPAddress *ip=NULL, IPAddress *dns=NULL, IPAddress *gateway=NULL, IPAddress *subnet=NULL) {
  // Do nothing if already connected
  if(WiFi.status() == WL_CONNECTED) return false;

  while(networkStatus != WL_CONNECTED) {
    networkStatus = WiFi.begin(NETWORK_SSID, NETWORK_PASSPHRASE);
    if(networkStatus == WL_CONNECTED) break;

    delay(10000);
  }

  // Flash the LED twice when connected to the network
  flashLed();
  flashLed();

  // Set a static IP if one was given
  if(ip != NULL && dns !=NULL && gateway != NULL && subnet != NULL) {
    WiFi.config(*ip, *dns, *gateway, *subnet);
  }

  return true;
}

void flashLed() {
  digitalWrite(LED_PIN, LOW);
  delay(LED_FLASH_DELAY);
  digitalWrite(LED_PIN, HIGH);
  delay(LED_FLASH_DELAY);
}

#endif
