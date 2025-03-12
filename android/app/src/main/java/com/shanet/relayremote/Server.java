package com.shanet.relayremote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Server {
  public String host;
  public int port;

  private Socket connSock     = null;
  private SocketAddress addr  = null;
  private BufferedReader recv = null;
  private PrintWriter send    = null;

  public Server() throws SocketException {
    this("", Constants.DEFAULT_PORT);
  }

  public Server(String host) throws SocketException {
    this(host, Constants.DEFAULT_PORT);
  }

  public Server(String host, int port) throws SocketException {
    this.host = host;
    this.port = port;

    // Init the socket this way so that the timeout is set before connecting
    addr = new InetSocketAddress(host, port);
    connSock = new Socket();
    connSock.setSoTimeout(Constants.NETWORK_TIMEOUT);
    connSock.setTcpNoDelay(true);
  }

  public int connect() throws UnknownHostException, IOException {
    connSock.connect(addr, Constants.NETWORK_TIMEOUT);

    if(connSock.isConnected()) {
      // Open send stream
      send = new PrintWriter(connSock.getOutputStream(), true);

      // Open read stream
      recv = new BufferedReader(new InputStreamReader(connSock.getInputStream()));

      if(send != null && recv != null) {
        return Constants.SUCCESS;
      }
    }

    return Constants.FAILURE;
  }

  public int send(String data) {
    // Only try to send if send isn't null
    if(send != null) {
      send.println(data);
      return Constants.SUCCESS;
    }

    return Constants.FAILURE;
  }

  public String receive() throws IOException {
    // Only try to receive if recv isn't null
    if(recv != null) {
      return recv.readLine();
    }

    return null;
  }

  public void close() throws IOException {
    if(send != null) {
      send.close();
    }

    if(recv != null) {
      recv.close();
    }

    if(connSock != null) {
      connSock.close();
    }
  }

  public boolean isConnected() {
    return connSock.isConnected();
  }

  public String getServerIPAddress() {
    if(isConnected()) {
      return connSock.getInetAddress().toString();
    }

    return "";
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
