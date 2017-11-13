#include "relay.h"

void setupRelayPins() {
  // Set the relay pins as output
  for(int i=MIN_RELAY_PIN; i<=MAX_RELAY_PIN; i++) {
    pinMode(i, OUTPUT);
  }

  pinMode(LED_PIN, OUTPUT);
}

int setPin(Client &client) {
  char pin;
  char command;

  // Read and ignore the hypen seperator
  if(client.read() != '-') {
    abortClient(client);
    return FAILURE;
  }

  // Read the pin
  if((pin = client.read()) == -1) {
    abortClient(client);
    return FAILURE;
  }

  // Convert pin to an int
  pin -= '0';

  // Check that the pin is in the valid range
  if(pin < MIN_RELAY_PIN || pin > MAX_RELAY_PIN) {
    abortClient(client);
    return FAILURE;
  }

  // Read and ignore the hypen seperator
  if(client.read() != '-') {
    abortClient(client);
    return FAILURE;
  }

  // Read the command to perform
  if((command = client.read()) == -1) {
    abortClient(client);
    return FAILURE;
  }

  switch(command) {
    case '0':
      // Turn relay off
      pinLow(pin);
      break;
    case '1':
      // Turn relay on
      pinHigh(pin);
      break;
    case 't':
    case 'T':
      // Toggle relay state
      (pinStates[pin] == HIGH ? pinLow(pin) : pinHigh(pin));
      break;
    default:
      // Unexpected data from client
      abortClient(client);
      return FAILURE;
  }

  client.println(OK);
  return SUCCESS;
}

void pinHigh(int pin) {
  digitalWrite(pin, HIGH);
  pinStates[pin] = HIGH;
}

void pinLow(int pin) {
  digitalWrite(pin, LOW);
  pinStates[pin] = LOW;
}

int getPins(Client &client) {
  // Create a string with the status of each pin
  char status[64];
  char append[5];
  memset(status, '\0', 64);

  for(int i=MIN_RELAY_PIN; i<=MAX_RELAY_PIN; i++) {
    char pin = i + '0';
    char state = (pinStates[i] == HIGH ? '1' : '0');

    sprintf(append, "%c-%c;", pin ,state);
    strncat(status, append, 4);
  }

  // Send the status string to the client
  client.print(status);

  return SUCCESS;
}
