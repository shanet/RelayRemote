#define _XOPEN_SOURCE 700

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <getopt.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>

#define MAX_MSG_LEN  7
#define MAX_PORT_LEN 6
#define BUFFER       1024

#define DEFAULT_OP   's'
#define DEFAULT_CMD  '1'
#define DEFAULT_PIN  '9'
#define DEFAULT_PORT "2424"

#define FAILURE -1
#define SUCCESS 0

char *prog;

int connect_to_server(const char *host, const char *port, struct addrinfo *server_info);
void print_get_op(char *reply);
void print_help();
void print_version();
