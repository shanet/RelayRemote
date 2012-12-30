// RelayRemote
// Shane Tully (shane@shanetully.com)
// shanetully.com
// https://github.com/shanet/RelayRemote
//
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

#define MAX_MSG_LEN   6
#define MAX_PORT_LEN  6
#define BUFFER        1024

#define DEFAULT_OP    's'
#define DEFAULT_CMD   '1'
#define DEFAULT_PIN   '9'
#define DEFAULT_PORT  "2424"

#define FAILURE       -1
#define SUCCESS       0

char *prog;

int connect_to_server(const char *host, const char *port, struct addrinfo *server_info);

void print_help();
void print_version();