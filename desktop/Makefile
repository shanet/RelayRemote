BINARY=relayremote
VERSION = "\"1.0.0\""

all:
	gcc -std=c99 -O2 -Wall -Wextra -o $(BINARY) desktop.c -DVERSION=$(VERSION)

clean:
	rm $(BINARY)
