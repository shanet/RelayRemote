VERSION = "\"1.0.0\""

all:
	gcc -std=c99 -ggdb -Wall -Wextra -o c_client c_client.c -DVERSION=$(VERSION) -D_POSIX_C_SOURCE

clean:
	rm c_client
