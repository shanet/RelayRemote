RelayRemote
===========

#### Kira Tully
#### ephemeral.cx

An Arduino-based 120V relay control server and Android client.

Note: This project may go long periods of time without being updated, however it is actively used and maintained. Bug reports are welcome.

## About

RelayRemote is an Arduino-based server for remotely controlling electrical relays which turn on/off devices run from a standard 120V AC circuit. The project includes four clients:

* An Android application
  * The Android app supports multiple relay servers, relay groups, homescreen widgets, and NFC tags.
* A web UI
* A desktop Rust client
* An Arduino-based wall switch

A demo video and brief description of how it works is available at: https://ephemeral.cx/2012/12/controlling-a-relay-via-an-arduino-from-an-android-client-with-nfc/

## Hardware

### Relay

RelayRemote was built and tested on an Adafruit Feather M0 WiFi. Any IoT 120V relay should work.

* [Adafruit Feather M0 WiFi](https://www.adafruit.com/products/3010)
* [Adafruit Feather Female Headers](https://www.adafruit.com/product/2886)
* [120V relay](https://www.amazon.com/dp/B00WV7GMA2), or similar
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

1. Connect a wire from the positive terminal of the relay to a pin between 2 and 9 (inclusive) on the Feather. Keep note of the pin you choose. Multiple relays can be connected to the same Feather by connecting them to different pins.
1. Connect a wire from the negative terminal of the relay to a ground pin on the Feather.

### Configuration

Copy `cp arduino/config.yml.default arduino/config.yml` for a sample relay configuration.

The config file supports multiple relay configurations. The types are:

* `wifi_server`: A relay board
* `wifi_client`: A wall-switch board
* `wired_server`: An older relay using an Arduino Uno with an Ethernet Shield. This is still supported for that type of board.

Each relay must have its networking configuration filled out and pin configuration for connected devices. The names are for the web UI and the Makefile `RELAY` argument to determine with configuration to use.

### Compiling & Uploading

These instructions are for Linux.

```
cd arduino
scripts/download_dependencies.sh
make wifi_server RELAY=wired_server_1 # Assumes the board is on /dev/ttyACM0, see the variable at the top of the Makefile if a different device is necessary.
```

### Android app

The Android app can be compiled by:

1. Install the Android SDK (http://developer.android.com/sdk/index.html).
1. Create `local.properties` in the `android/` directory with the following contents: `sdk.dir=/path/to/android/sdk`.
1. From the `android` directory, run:

```
./gradlew assembleDebug
./gradlew installDebug
```

### Web UI

Each relay serves a web UI for itself. Open `http://[relay-ip]` to access it. *Note that it is HTTP, not HTTPS.*

### Desktop client

For control of relays from desktop systems, a command line Rust program is provided.

To build, run `cargo build` from the `desktop/` directory. The resulting binary will be in `desktop/target/debug`. Use `--help` for usage instructions.

### Wall Switch

1. Wire the circuit as follows:
  ![](/arm/docs/wiring.png?raw=true)
  On the switch VDD is pin 1 (left most) and LED- is pin 4 (right most).
1. The follow the same build instructions as above with 
1. Use `make wifi_client RELAY=wifi_client_1` to build.

### Networking

All communication happens over port 80. Don't forget to add rules to allow communication on this port on any firewalls or gateways between the client and the server.

## License

GPLv3
