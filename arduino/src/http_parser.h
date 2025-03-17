#ifndef HTTP_PARSER_H
#define HTTP_PARSER_H

#include <Arduino.h>
#include "constants.h"

void parseRequestLine(String &headers, String &method, String &route, String &httpVersion);
String toLower(String input);

template <typename T>
bool readRequest(T &client, String &headers, String &body) {
  String currentLine;
  bool readingBody = false;
  int readBytes = 0;
  int contentLength = 0;
  unsigned long lastReadTime = millis();

  while(client.connected() && (millis() - lastReadTime < READ_TIMEOUT)) {
    if(!client.available()) continue;

    currentLine += (char)client.read();
    readBytes += sizeof(char);
    lastReadTime = millis();

    if(readBytes >= MAX_REQUEST_LENGTH) return 413;

    // Switch to reading the body or stop once the headers are finished
    if(!readingBody && (currentLine == F("\r\n") || currentLine == F("\n"))) {
      readingBody = true;

      // If we reached the body and there's still no content length header found then it must be a GET request so stop reading here.
      // Otherwise, it's a POST request without a content length header which is not supported here so don't try to read more anyway.
      if(contentLength <= 0) break;

      // Skip to the next iteration so we don't append the separator line to the body output
      currentLine = "";
      continue;
    }

    // Process the end of the line when reading headers
    if(!readingBody && currentLine.endsWith("\n")) {
      // Store the content length so we know how much to read for the body
      String currentLineLowercase = toLower(currentLine);

      if(currentLineLowercase.startsWith(F("content-length: "))) {
        int contentLengthIndex = currentLineLowercase.indexOf(F("content-length: ")) + 16;
        contentLength = currentLine.substring(contentLengthIndex).toInt();
      }

      // On AVR boards with limited memory it seems that it's possible to run out of memory when parsing requests with many
      // headers (like thos generated from web browsers). We really only care about the request line though so append that
      // first line and then ignore everything else. If AVR support is ever dropped this condition could probably be removed.
      if(headers == "") {
        headers += currentLine;
      }

      currentLine = "";
      continue;
    }

    // Process the of the line when reading the body or when the content length is hit
    if(readingBody && (currentLine.endsWith("\n") || body.length() + currentLine.length() >= contentLength)) {
      body += currentLine;
      Serial.println(body);

      // If we reached the content length stop reading data
      if(body.length() >= contentLength) break;

      currentLine = "";
      continue;
    }
  }

  headers.trim();
  body.trim();

  return 0;
}

void parseRequestLine(String &headers, String &method, String &route, String &httpVersion) {
  String requestLine = headers.substring(0, headers.indexOf('\n'));
  requestLine.trim();

  int routeIndex = requestLine.indexOf(' ') + 1;
  int httpVersionIndex = requestLine.indexOf(' ', routeIndex) + 1;

  method = requestLine.substring(0, routeIndex - 1);
  route = requestLine.substring(routeIndex, httpVersionIndex - 1);
  httpVersion = requestLine.substring(httpVersionIndex);
}

String toLower(String input) {
  String output = input;

  for(int i=0; i<input.length(); i++) {
    output[i] = tolower(input[i]);
  }

  return output;
}

#endif
