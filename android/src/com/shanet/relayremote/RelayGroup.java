// Copyright (C) 2012 Shane Tully 
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

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
