#if defined(WIRED_SERVER) || defined(WIFI_SERVER)
#ifndef HTTP_RESPONSES_H
#define HTTP_RESPONSES_H

#include <Arduino.h>
#include "constants.h"
#include "http_parser.h"

// Get the IPAddress class
#ifdef WIFI_SERVER
  #include <WiFi101.h>
#elif defined(WIRED_SERVER)
  #include <Ethernet.h>
#endif

#define MAX_REQUEST_LENGTH 1024 // chars

const char PINS[] = {CONFIGURED_PINS};

const String PIN_LABELS[] = {
  PIN_0_LABEL, // Not used
  PIN_1_LABEL, // Not used
  PIN_2_LABEL,
  PIN_3_LABEL,
  PIN_4_LABEL,
  PIN_5_LABEL,
  PIN_6_LABEL,
  PIN_7_LABEL,
  PIN_8_LABEL,
  PIN_9_LABEL,
};

#ifdef WIRED_SERVER
  #include <Ethernet.h>
#endif

void parseRequestLine(String &headers, String &method, String &route, String &httpVersion);
String toLower(String input);

extern char getPinState(char pin);
extern void setPinState(char pin, char state);
extern void togglePinState(char pin);

template <typename T>
void handleRequest(T &client) {
  String headers;
  String method;
  String route;
  String httpVersion;
  String body;

  char readStatus = readRequest(client, headers, body);

  if(readStatus == 413) {
    sendError(client, F("413 Payload Too Large"), "Max request length is " + String(MAX_REQUEST_LENGTH) + " bytes.");
    return;
  } else if(readStatus != 0) {
    // This shouldn't happen
    return;
  }

  parseRequestLine(headers, method, route, httpVersion);

  if(httpVersion != "HTTP/1.1") {
    sendError(client, "505 HTTP Version Not Supported", httpVersion + " received but only HTTP/1.1 is supported.");
    return;
  }

  Serial.println("Received request: " + method + " " + route);

  if(method == F("GET") && route == F("/")) {
    routeIndex(client);
  } else if(method == F("GET") && route == F("/api/pins")) {
    routeApiIndex(client);
  } else if(method == F("POST") && route.startsWith(F("/api/pins/"))) {
    routeApiEdit(client, route, body);
  } else {
    sendError(client, F("404 Not Found"), route + " not found");
    return;
  }

  // We're done with this client. Disconnect it.
  client.stop();
}

template <typename T>
void routeIndex(T &client) {
  sendResponse(client, F("200 OK"), "", F("text/html"));

  client.println(F("<html>"));
  client.println(F("  <head>"));
  client.println(F("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1\">"));
  client.println(F(""));
  client.println(  "    <title>" + String(RELAY_NAME) + " Relay</title>");
  client.println(F(""));
  client.println(F("    <style>"));
  client.println(F("      body {"));
  client.println(F("        background-color: #333;"));
  client.println(F("        color: #eee;"));
  client.println(F("        font-family: sans-serif;"));
  client.println(F("        margin: 0;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      header {"));
  client.println(F("        background: linear-gradient(180deg, #1976D2 0%, #333 100%);"));
  client.println(F("        font-weight: bold;"));
  client.println(F("        padding: 1rem 1rem 1.5rem 1rem;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      ul {"));
  client.println(F("        margin-left: 1rem;"));
  client.println(F("        padding: 0;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      p {"));
  client.println(F("        margin-right: 1rem;"));
  client.println(F("        min-width: 5rem;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      form {"));
  client.println(F("        display: inline-block"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      form input[type=\"submit\"] {"));
  client.println(F("        color: #eee;"));
  client.println(F("        font-weight: bold;"));
  client.println(F("        padding: 0.75rem;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      form input[type=\"submit\"]:hover {"));
  client.println(F("        cursor: pointer;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      form input[type=\"submit\"]:not([disabled]) {"));
  client.println(F("        background-color: transparent !important;"));
  client.println(F("        border: 1px solid #eee !important;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      form input[type=\"submit\"].on {"));
  client.println(F("        background-color: #28a745;"));
  client.println(F("        border-radius: 0.3rem 0 0 0.3rem;"));
  client.println(F("        border-right: none !important;"));
  client.println(F("        border: 2px solid #28a745;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      form input[type=\"submit\"].off {"));
  client.println(F("        background-color: #dc3545;"));
  client.println(F("        border-left: none !important;"));
  client.println(F("        border-radius: 0 0.3rem 0.3rem 0;"));
  client.println(F("        border: 1px solid #dc3545;"));
  client.println(F("      }"));
  client.println(F(""));
  client.println(F("      .flex {"));
  client.println(F("        align-items: baseline;"));
  client.println(F("        display: flex;"));
  client.println(F("        flex-wrap: wrap;"));
  client.println(F("      }"));
  client.println(F("    </style>"));
  client.println(F("  </head>"));
  client.println(F(""));
  client.println(F("  <body>"));
  client.println(F("    <header>"));
  client.println(  "      " + String(RELAY_NAME));
  client.println(F("    </header>"));
  client.println(F(""));
  client.print(F(  "    <ul>"));

  for(int index=0; index<(sizeof(PINS)/sizeof(PINS[0])); index++) {
    int pin = PINS[index];
    int state = getPinState(pin);

    client.println("");
    client.println(F("      <div class=\"flex\">"));
    client.println(  "        <p>" + PIN_LABELS[pin] + ":</p>");
    client.println("");
    client.println(  "        <form action=\"/api/pins/" + String(pin) + "\" method=\"POST\">");
    client.println(F("          <input type=\"hidden\" name=\"state\" value=\"on\">"));
    client.println(  "          <input type=\"submit\" class=\"on\" value=\"On\"" + String(state == HIGH ? " disabled" : "") + ">");
    client.println(F("        </form>"));
    client.println("");
    client.println(  "        <form action=\"/api/pins/" + String(pin) + "\" method=\"POST\">");
    client.println(F("          <input type=\"hidden\" name=\"state\" value=\"off\">"));
    client.println(  "          <input type=\"submit\" class=\"off\" value=\"Off\"" + String(state == LOW ? " disabled" : "") + ">");
    client.println(F("        </form>"));
    client.println(F("      </div>"));
  }

  client.println(F("    </ul>"));
  client.println(F("  </body>"));
  client.println(F("</html>"));
}

template <typename T>
void routeApiIndex(T &client) {
  sendResponse(client, F("200 OK"), "", F("application/json"));

  client.print("{");

  int pins_length = sizeof(PINS)/sizeof(PINS[0]);

  for(int index=0; index<pins_length; index++) {
    int pin = PINS[index];
    int state = getPinState(pin);

    client.print("\"" + String(pin) + "\":" + (state == HIGH ? "1" : "0") + (index == pins_length-1 ? "" : ","));
  }

  client.println("}");
}

template <typename T>
void routeApiEdit(T &client, String route, String body) {
  int pinIndex = route.lastIndexOf("/") + 1;
  int pin = route.substring(pinIndex).toInt();

  if(pin == 0) {
    sendResponse(client, F("400 Bad Request"), F("Invalid pin number"));
    client.stop();
    return;
  }

  int stateIndex = body.indexOf(F("state=")) + 6;
  String state = body.substring(stateIndex);

  if(state == F("on")) {
    setPinState(pin, HIGH);
  } else if(state == F("off")) {
    setPinState(pin, LOW);
  } else if(state == F("toggle")) {
    togglePinState(pin);
  } else {
    sendError(client, F("400 Bad Request"), "Invalid state, must be either \"on\", \"off\", or \"toggle\". \"" + state + "\" received");
    return;
  }

  sendRedirect(client, "/");
}

template <typename T>
void sendResponse(T &client, String status, String body, String content_type="text/plain") {
  client.println("HTTP/1.1 " + status);
  client.println("Content-Type: " + content_type);
  client.println(F("Connection: close"));
  client.print(F("\r\n"));

  if(body != "") client.println(body);
}

template <typename T>
void sendError(T &client, String status, String body) {
  sendResponse(client, status, body);
  client.stop();
}

template <typename T>
void sendRedirect(T &client, String location) {
  client.println(F("HTTP/1.1 303 See Other"));
  client.println("Location: " + location);
  client.println(F("Content-Length: 0"));
  client.println(F("Connection: close"));
  client.print(F("\r\n"));
}

#endif
#endif
