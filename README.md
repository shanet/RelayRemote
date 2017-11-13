RelayRemote
===========

#### Shane Tully (shanetully.com)

An Arduino-based relay control server and Android client

Note: This project may go long periods of time without being updated, however it is actively used and maintained. Bug reports and pull requests are welcome.

## About

RelayRemote is an Arduino-based server for remotely controlling electrical relays which in turn turn on/off devices run from a standard 120V AC circuit. The project includes three clients, an Android application, a desktop client for control from Linux systems, and an Arduino-based wall switch.

The Android app supports multiple relay servers, relay groups, homescreen widgets, and NFC tags.

A demo video and brief description of how it works is available at: https://shanetully.com/2012/12/controlling-a-relay-via-an-arduino-from-an-android-client-with-nfc/

The original version of this project used an Arduino Uno and Arduino ethernet shield. See [the legacy documentation](arduino/docs/README.md) for instructions on using the legacy hardware.

## Hardware

### Relay

RelayRemote was built and tested on an Adafruit Feather M0 WiFi. A PowerSwitch Tail II is the recommended relay.

* [Adafruit Feather M0 WiFi](https://www.adafruit.com/products/3010)
* [Adafruit Feather Female Headers](https://www.adafruit.com/product/2886)
* [PowerSwitch Tail II](http://www.powerswitchtail.com/)
* [5V 1A (1000mA) USB port power supply](https://www.adafruit.com/products/501)
* A micro-usb cable

### Wall Switch

An optional piece of hardware is a wall switch for turning on/off relays more like a traditional light switch.

* [Adafruit Feather M0 WiFi](https://www.adafruit.com/products/3010)
* [Short Feather Headers Kit](https://www.adafruit.com/products/2940)
* [FeatherWing Proto](https://www.adafruit.com/products/2884)
* [Illuminated Pushbutton Switch](http://www.mouser.com/ProductDetail/VCC/CTHS15CIC05ONOFF/?qs=sGAEpiMZZMufv8JNQ5fVHWY5rHfF8YY%252brvRHU%252b5jXA4c%252b8xo7kTh0w%3d%3d)
* [5V 1A (1000mA) USB port power supply](https://www.adafruit.com/products/501)
* 330 Ohm resistor (or similar)
* A micro-usb cable

## Usage

### Setting up the hardware

0. Connect a wire from the positive terminal of the relay to a pin between 2 and 9 (inclusive) on the Feather. Keep note of the pin you choose. Multiple relays can be connected to the same Feather by connecting them to different pins.
0. Connect a wire from the negative terminal of the relay to a ground pin on the Feather.

### Setting up the software dependencies

These instructions are for Linux (x86_64).

* Download and extract the [Adafruit SAMD library](https://github.com/adafruit/arduino-board-index/raw/gh-pages/boards/adafruit-samd-1.0.9.tar.bz2) to `~/.arduino15/packages/adafruit/hardware/samd/1.0.9`
* Download and extract the [ARM compiler](http://downloads.arduino.cc/gcc-arm-none-eabi-4.8.3-2014q1-linux64.tar.gz) to `~/.arduino15/packages/adafruit/tools/arm-none-eabi-gcc/4.8.3-2014q1`
* Download and extract [Bossac](http://downloads.arduino.cc/bossac-1.6.1-arduino-x86_64-linux-gnu.tar.gz) to `~/.arduino15/packages/adafruit/tools/bossac/1.6.1-arduino`.
* Download and extract [CMSIS](http://downloads.arduino.cc/CMSIS-4.0.0.tar.bz2) to `~/.arduino15/packages/adafruit/tools/CMSIS/4.0.0-atmel`.

Note: The archives above can be extracted whenever you'd like, but the paths at the top of the Makefile must be adjusted accordingly.

### Compiling & Uploading

0. Create `arm/src/secrets.h` with the following content:

```
#define _SSID "your_ssid"
#define _PASSPHRASE "passphrase"
```

0. Change the network settings (IP, DNS, netmask, and gateway) for the Feather by editing the server header (`arm/server.h`) in the `arm` directory of this repo.
0. Then:

```
$ cd arm
$ make WIFI_SERVER
$ make upload
```

### Wall Switch

0. Wire the circuit as follows:
   ![](/arm/docs/wiring.png?raw=true)
   On the switch VDD is pin 1 (left most) and LED- is pin 4 (right most).
0. Then follow the instructions in the "Setting up the software dependencies" and "Compiling & Uploading" sections above.
0. Use `make WIFI_CLIENT` to build.

### Android app

0. Install the APK provided on the Releases page

Alternatively, the Android app can be compiled by:

0. Install the Android SDK (http://developer.android.com/sdk/index.html).
0. Create `local.properties` in the `android/` directory with the following contents: `sdk.dir=/path/to/android/sdk`.
0. From the `android` directory, run:
```
$ ./gradlew assembleDebug
$ ./gradlew installDebug
```

### Desktop client

0. If desired, build the desktop client in the `desktop` directory of this repo on a Linux system by running `make` from that directory.
0. The desktop client is fairly straightforward. Run it with `--help` for more info.

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
