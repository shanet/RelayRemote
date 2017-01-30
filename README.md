RelayRemote
===========

#### Shane Tully (shanetully.com)

An Arduino relay control server and Android client

Note: Despite not being updated for years, this project is actively maintained. Bug reports and pull requests are welcome.

## About

RelayRemote is an Arduino-based server for turning off and on electrical relays, which turn on/off devices run from a standard 120V AC circuit. The project includes two clients, an Android application and a C client for control from Linux systems.

The Android app supports multiple relay servers, relay groups, homescreen widgets, and NFC tags.

A demo video and brief description of how it works is available at: https://shanetully.com/2012/12/controlling-a-relay-via-an-arduino-from-an-android-client-with-nfc/

## Hardware

### Relay

RelayRemote was built and tested on an Arduino Uno with the Arduino Ethernet Shield for networking capabilities. A PowerSwitch Tail II is the recommended relay.

* [Arduino Uno](http://www.arduino.cc/en/Main/arduinoBoardUno)
* [Arduino Ethernet Shield](http://www.arduino.cc/en/Main/ArduinoEthernetShield)
* [PowerSwitch Tail II](http://www.powerswitchtail.com/Pages/default.aspx)

### Wall Switch

Another optional piece of hardware is a wall switch for turning on/off relays more like a traditional light switch.

* [Adafruit Feather M0 WiFi](https://www.adafruit.com/products/3010)
* [Short Feather Headers Kit](https://www.adafruit.com/products/2940)
* [FeatherWing Proto](https://www.adafruit.com/products/2884)
* [Illuminated Pushbutton Switch](http://www.mouser.com/ProductDetail/VCC/CTHS15CIC05ONOFF/?qs=sGAEpiMZZMufv8JNQ5fVHWY5rHfF8YY%252brvRHU%252b5jXA4c%252b8xo7kTh0w%3d%3d)
* [5V 1A (1000mA) USB port power supply](https://www.adafruit.com/products/501)
* 330Ohm resistor (or close)
* A micro-usb cable

## Usage

### Setting up the hardware

0. Connect the Arduino ethernet shield to the Arduino by placing the ethernet shield on top of the Arduino
0. Connect a wire from the positive terminal of the relay to a pin between 2 and 9 (inclusive) on the Arduino. Keep note of the pin you choose. Multiple relays can be connected to the same Arduino by connecting them to different pins.
0. Connect a wire from the negative terminal of the relay to a ground pin on the Arduino
0. Connect the Arduino to your network

### Installing the software

0. Change the network settings (IP, MAC, netmask, and gateway) for the Arduino by editing the server sketch (relay.pde) in the arduino folder of this repo. The lines needing changes are near the top of the file and are marked by a comment.
0. Use the Arduino software (http://arduino.cc/en/main/software) to compile and load the server sketch (relay.pde) in the arduino folder of this repo to the Arduino.
0. Repeat steps 1 and 2 for each Arduino you're setting up
0. Install the Android SDK (http://developer.android.com/sdk/index.html).
0. Install the APK provided on the Releases page or compile it yourself (see the compiling section)
0. If desired, build the C client in the c_client folder of this repo on a Linux system by running `make` in a terminal.

### Wall Switch Circuit

Wire the circuit as follows:

![](/arm/docs/wiring.png?raw=true)

On the switch VDD is pin 1 (left most) and LED- is pin 4 (right most).

#### Software Dependencies

These instructions are for Linux (x86_64). See below for where to find instructions for other platforms. Do not ask for Windows support.

* Download and extract the [Adafruit SAMD library](https://github.com/adafruit/arduino-board-index/raw/gh-pages/boards/adafruit-samd-1.0.9.tar.bz2) to `~/.arduino15/packages/adafruit/hardware/samd/1.0.9`
* Download and extract the [ARM compiler](http://downloads.arduino.cc/gcc-arm-none-eabi-4.8.3-2014q1-linux64.tar.gz) to `~/.arduino15/packages/adafruit/tools/arm-none-eabi-gcc/4.8.3-2014q1`
* Download and extract [Bossac](http://downloads.arduino.cc/bossac-1.6.1-arduino-x86_64-linux-gnu.tar.gz) to `~/.arduino15/packages/adafruit/tools/bossac/1.6.1-arduino`.
* Download and extract [CMSIS](http://downloads.arduino.cc/CMSIS-4.0.0.tar.bz2) to `~/.arduino15/packages/adafruit/tools/CMSIS/4.0.0-atmel`.

Note: The archives above can be extracted whenever you'd like, but the paths at the top of the Makefile must be adjusted accordingly.

Alternatively, Adafruit has good instructions for getting the above set up with the help of the Arduino software. If not using Linux, this is most likely the easiest way to gather the dependencies.

#### Compiling & Uploading

Create `arm/src/secrets.h` with the following content:

```
#define SSID "your_ssid"
#define PASSPHRASE "passphrase"
```

Then:

```
$ cd arm
$ make
$ make upload
```

### Using the Android app

0. After opening the Android app, relays must be added.
0. After adding relays, they can be added to a group, a widget can be placed on the homescreen, or an NFC tag can be created to turn the relays on/off (if the device supports NFC)

### Using the C client

0. The C client is fairly straightforward. Run it with `--help` for more info.

### Note

The default port is 2424. Don't forget to add rules to allow communication on this port on any firewalls or gateways between the client and the server.

### Compiling

To compile the Android app yourself:

0. Install the Android SDK
0. Create `local.properties` in the `android/` directory with the following contents: `sdk.dir=/path/to/android/sdk`
0. From the `android/` directory, run `./gradlew assembleDebug` to build or `./gradlew installDebug` to build and install to attached device.

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
