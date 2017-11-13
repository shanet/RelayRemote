RelayRemote
===========

This is the legacy documentation for the first version of RelayRemote which used an Arduino Uno and Arduino Ethernet Sheild. The code is still compatible with the existing Android app and desktop client. See the main README for instructions on using the clients.

## Hardware

### Relay

RelayRemote was built and tested on an Arduino Uno with the Arduino Ethernet Shield for networking capabilities. A PowerSwitch Tail II is the recommended relay.

* [Arduino Uno](http://www.arduino.cc/en/Main/arduinoBoardUno)
* [Arduino Ethernet Shield](http://www.arduino.cc/en/Main/ArduinoEthernetShield)
* [PowerSwitch Tail II](http://www.powerswitchtail.com/)

## Usage

### Setting up the hardware

1. Connect the Arduino ethernet shield to the Arduino by placing the ethernet shield on top of the Arduino.
1. Connect a wire from the positive terminal of the relay to a pin between 2 and 9 (inclusive) on the Arduino. Keep note of the pin you choose. Multiple relays can be connected to the same Arduino by connecting them to different pins.
1. Connect a wire from the negative terminal of the relay to a ground pin on the Arduino.
1. Connect the Arduino to your network.

### Installing the software

1. Change the network settings (IP, MAC, netmask, and gateway) for the Arduino by editing the server sketch (`relay.pde`) in the `arduino` directory of this repo. The lines needing changes are near the top of the file and are marked by a comment.
1. Use the Arduino software (http://arduino.cc/en/main/software) to compile and load the server sketch (`relay.pde`) in the `arduino` directory of this repo to the Arduino.
1. Repeat steps 1 and 2 for each Arduino you're setting up.

### Networking

The default port is 2424. Don't forget to add rules to allow communication on this port on any firewalls or gateways between the client and the server.

## License

Copyright (C) 2012 Shane Tully

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
