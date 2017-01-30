#include <SPI.h>
#include <Ethernet.h>

#define PORT 2424
#define OK   "OK"
#define ERR  "ERR"

#define SUCCESS 0
#define FAILURE -1

// IMPORTANT: The IP AND MAC MUST BE CHANGED to something unique for each Arduino.
// The gateway will probably need changed as well.
byte ip[]      = {10, 10, 10, 31};
byte mac[]     = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xEE};
byte gateway[] = {10, 10, 10, 1};
byte subnet[]  = {255, 255, 255, 0};

EthernetServer server = EthernetServer(PORT);

void setup() {
   // Set the relay pins as output
   for(int i=2; i<=9; i++) {
      pinMode(i, OUTPUT);
   }

   // Start the server
   Ethernet.begin(mac, ip, gateway, subnet);
   server.begin();
}

void loop() {
   // The client should be sending one of two commands.
   // GET: Of the form "g". Tells us to send back the status of each pin
   // SET: Of the form "s-[pin]-[state]". Tells us to set [pin] to [state]
   //      Pin should be any pin in [2,9]
   //      State is either 0 (off), 1 (on), or t (toggle)
   char op;

   // Get a client from the server
   EthernetClient client = server.available();

   if(client) {
      if(client.available()) {
         // Read the operation
         op = client.read();

         switch(op) {
            // Get status operation
            case 'g':
               op_get(client);
               break;
            // Set pin operation
            case 's':
               if(op_set(client) == FAILURE) return;
               break;
            default:
               abort_client(client);
               return;
         }
      }

      // We're done with this client. Disconnect it.
      client.stop();
   }
}

int op_set(EthernetClient client) {
   char pin;
   char cmd;

   // Read and ignore the hypen seperator
   if(client.read() != '-') {
      abort_client(client);
      return FAILURE;
   }

   // Read the pin
   if((pin = client.read()) == -1) {
      abort_client(client);
      return FAILURE;
   }

   // Check that the pin is in the valid range
   if(pin-48 < 2 || pin-48 > 9) {
      abort_client(client);
      return FAILURE;
   }

   // Read and ignore the hypen seperator
   if(client.read() != '-') {
      abort_client(client);
      return FAILURE;
   }

   // Read the command to perform
   if((cmd = client.read()) == -1) {
      abort_client(client);
      return FAILURE;
   }

   // Convert pin to an int
   pin -= 48;

   switch(cmd) {
      // Turn relay off
      case '0':
         digitalWrite(pin, LOW);
         client.println(OK);
         break;
      // Turn relay on
      case '1':
         digitalWrite(pin, HIGH);
         client.println(OK);
         break;
      // Toggle relay state
      case 't':
      case 'T':
         (digitalRead(pin) == HIGH) ? digitalWrite(pin, LOW) : digitalWrite(pin, HIGH);
         client.println(OK);
         break;
      // Unexpected data from client
      default:
         abort_client(client);
         return FAILURE;
   }

   return SUCCESS;
}

int op_get(EthernetClient client) {
   // Create a string with the status of each pin
   char status[34];
   char append[5];
   status[0] = '\0';

   for(int i=2; i<=9; i++) {
      sprintf(append, "%c-%c;", i+48, (digitalRead(i) == HIGH) ? '1' : '0');
      strncat(status, append, 4);
   }

   // Add a final newline and move the nul
   status[32] = '\n';
   status[33] = '\0';

   // Send the status string to the client
   client.print(status);

   return SUCCESS;
}

void abort_client(EthernetClient client) {
   client.println(ERR);
   client.stop();
}
