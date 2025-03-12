package com.shanet.relayremote;

import java.util.ArrayList;

public class RelayGroup {
  private int gid;
  private String name;
  private ArrayList<Integer> rids;
  private boolean isOn;

  public RelayGroup() {
    this("", null);
  }

  public RelayGroup(String name, ArrayList<Integer> rids) {
    this(-1, name, rids, false);
  }

  public RelayGroup(int gid, String name, ArrayList<Integer> rids, boolean isOn) {
    this.gid  = gid;
    this.name = name;
    this.rids = rids;
    this.isOn = isOn;
  }

  public int getGid() {
    return gid;
  }

  public String getName() {
    return name;
  }

  public ArrayList<Integer> getRids() {
    return rids;
  }

  public boolean isOn() {
    return isOn;
  }

  public void setGid(int gid) {
    this.gid = gid;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRids(ArrayList<Integer> rids) {
    this.rids = rids;
  }

  public void turnOn() {
    isOn = true;
  }

  public void turnOff() {
    isOn = false;
  }

  public String toString() {
    return "GID: " + gid + "\nName: " + name + "\nRids: " + rids.toString() + "\nis on? " + isOn;
  }
}
