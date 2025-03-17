#ifndef HTTP_REQUESTS_H
#define HTTP_REQUESTS_H

#include "http_parser.h"

template <typename T>
String sendRequest(T &client, String &method, String &route, String &body) {
  client.println(method + " " + route + " HTTP/1.1");
  client.println("Content-Length: " + String(body.length() + 1));
  client.print(F("\r\n"));

  if(body != "") client.println(body);

  // Read the response
  String headers;
  String responseBody;

  if(readRequest(client, headers, responseBody) != 0) return "";

  return headers;
}

#endif
