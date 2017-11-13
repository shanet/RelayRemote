#include "network.h"

#if defined(WIFI_SERVER) || defined(WIFI_CLIENT)
  void wifiSetup() {
    WiFi.setPins(8, 7, 4, 2);
  }

  void connectToNetwork(IPAddress *ip=NULL, IPAddress *dns=NULL, IPAddress *gateway=NULL, IPAddress *subnet=NULL) {
    while(networkStatus != WL_CONNECTED) {
      networkStatus = WiFi.begin(_SSID, _PASSPHRASE);
      delay(10000);
    }

    // Flash the LED twice when connected to the network
    flashLed();
    flashLed();

    // Set a static IP if one was given
    if(ip != NULL && dns !=NULL && gateway != NULL && subnet != NULL) {
      WiFi.config(*ip, *dns, *gateway, *subnet);
    }
  }
#endif

void processMessage(Client &client) {
  // The client should be sending one of two commands.
  // GET: Of the form "g". Tells us to send back the status of each pin
  // SET: Of the form "s-[pin]-[state]". Tells us to set [pin] to [state]
  //      Pin should be any pin in [MIN_PIN, MAX_PIN]
  //      State is either 0 (off), 1 (on), or t (toggle)

  // Read the operation
  char operation = client.read();

  switch(operation) {
    case 'g':
      // Get status operation
      getPins(client);
      return;
    case 's':
      // Set pin operation
      setPin(client);
      return;
    default:
      abortClient(client);
      return;
  }
}

void abortClient(Client &client) {
   client.println(ERR);
   client.stop();
}

void flashLed() {
  digitalWrite(LED_PIN, LOW);
  delay(LED_FLASH_DELAY);
  digitalWrite(LED_PIN, HIGH);
  delay(LED_FLASH_DELAY);
}
