package com.shanet.relayremote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {

	private static final int DEFAULT_PORT = 4242;
	private static final int SUCCESS      = 0;
	private static final int FAILURE      = -1;
	
	public String host;
	public int port;

    private Socket connSock     = null;
    private BufferedReader recv = null;
    private PrintWriter send    = null;
    
	public Server(String host, int port) {
		this.host = host;
		this.port = port;
		
		// Init connSock without a host and port so that if isConnected() is called, it will
		// return false rather than thrown a null pointer exception
		connSock = new Socket();
	}
	
	public Server(String host) {
		this(host, DEFAULT_PORT);
	}
	
	
	public Server() {
		this("", DEFAULT_PORT);
	}
	
	
    public int connect() throws UnknownHostException, IOException {
    	connSock = new Socket(host, port);
    	
    	// Set a timeout on the socket
    	//connSock.setSoTimeout(Constants.NETWORK_TIMEOUT);
    	
    	if(connSock.isConnected()) {
    	   // Open send stream
           send = new PrintWriter(connSock.getOutputStream(), true);

           // Open read stream
           recv = new BufferedReader(new InputStreamReader(connSock.getInputStream()));
           
           if(send != null && recv != null)
              return SUCCESS;
    	}
    	return FAILURE;
    }
    

    public int send(String data) {
    	// Only try to send if send isn't null
    	if(send != null) {
    	   send.println(data);
    	   return SUCCESS;
    	}
    	
    	return FAILURE;
    }

    
    public String receive() throws IOException {
    	// Only try to receive if recv isn't null
    	if(recv != null) {
    		return recv.readLine();
    	}

    	return null;
    }

    
    public void close() throws IOException {
    	if(send != null)
    		send.close();
    	if(recv != null)
    		recv.close();
    	if(connSock != null)
    		connSock.close();
    }

    
    public boolean isConnected() {
    	return connSock.isConnected();
    }
    
    
    public String getServerIPAddress() {
    	if(isConnected())
    		return connSock.getInetAddress().toString();
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