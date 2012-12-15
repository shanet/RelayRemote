package com.shanet.relayremote;

public class Relay {
	
	private int rid;
	private String name;
	private int pin;
	private String server;
	private int port;
	private boolean isOn;
	
	public Relay() {
		this("", Constants.DEFAULT_PIN, "", Constants.DEFAULT_PORT);
	}
	
	public Relay(String name, int pin, String server) {
		this(name, pin, server, Constants.DEFAULT_PORT);
	}
	
	public Relay(String name, int pin, String server, int port) {
		this(-1, name, pin, server, port, false);
	}
	
	public Relay(int rid, String name, int pin, String server, int port, boolean isOn) {
		this.rid    = rid;
		this.name   = name;
		this.pin    = pin;
		this.server = server;
		this.port   = port;
		this.isOn   = isOn;
	}
	
	public int getRid() {
		return rid;
	}
	
	public String getName() {
		return name;
	}
	
	public int getPin() {
		return pin;
	}
	
	public String getServer() {
		return server;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean isOn() {
		return isOn;
	}
	
	public void setRid(int rid) {
		this.rid = rid;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPin(int pin) {
		this.pin = pin;
	}
	
	public void setServer(String server) {
		this.server = server;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setIsOn(boolean isOn) {
		this.isOn = isOn;
	}
	
	public String toString() {
		return "RID: " + rid + "\nName: " + name + "\nPin: " + pin + "\nServer: " + server + "\nPort: " + port + "\nIs On? " + isOn;
	}
}
