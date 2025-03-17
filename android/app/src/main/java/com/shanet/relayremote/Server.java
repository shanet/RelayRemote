package com.shanet.relayremote;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

public class Server {
  public String host;
  public int port;

  public Server() {
    this("", Constants.DEFAULT_PORT);
  }

  public Server(String host) {
    this(host, Constants.DEFAULT_PORT);
  }

  public Server(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String get(String path) throws IOException, MalformedURLException {
    URL url = buildUrl(path);
    HttpURLConnection request = (HttpURLConnection)url.openConnection();
    String response = readResponse(request);
    request.disconnect();

    if(request.getResponseCode() != 200) {
      throw new IOException("Request failed (" + request.getResponseCode() + "): " + response);
    }

    return response;
  }

  public void post(String path, String body) throws IOException, MalformedURLException {
    URL url = buildUrl(path);
    HttpURLConnection request = (HttpURLConnection)url.openConnection();
    request.setRequestMethod("POST");

    OutputStream output = request.getOutputStream();
    output.write(body.getBytes());

    String response = readResponse(request);
    request.disconnect();

    // Relays return redirects on successful POST requests
    if(request.getResponseCode() != 200) {
      throw new IOException("Request failed (" + request.getResponseCode() + "): " + response);
    }
  }

  private URL buildUrl(String path) throws MalformedURLException {
    return new URL("http://" + host + (port == Constants.DEFAULT_PORT ? "" : ":" + port) + path);
  }

  private String readResponse(HttpURLConnection request) throws IOException {
    InputStream input = new BufferedInputStream(request.getInputStream());
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

    StringBuilder response = new StringBuilder();
    String line = reader.readLine();

    while(line != null) {
      response.append(line);

      try {
        line = reader.readLine();
      } catch(SocketException exception) {
        // Ignore errors while reading data; we'll take what we can get
      }
    }

    return response.toString();
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setHost(String host) {
    this.host = host;
  }
}
