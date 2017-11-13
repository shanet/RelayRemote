#include "desktop.h"

int main(int argc, char **argv) {
  char op;                             // Operation to send to the server
  char cmd;                            // Command to send to the server
  char pin;                            // Pin to send to the server

  char message[MAX_MSG_LEN];           // Messsage to send to server
  char reply[BUFFER];                  // Reply from server
  int recv_len;                        // Length of reply from server

  char *host;                          // IP of the server
  char port[MAX_PORT_LEN];             // Port to connect on
  int socket;                          // Socket to use for network IO
  struct addrinfo *server_info = NULL; // Info about the server

  char c;                              // Char for processing command line args
  int opt_index;                       // Index of long opts for processing command line args

  // Set some default values
  sprintf(port, DEFAULT_PORT);
  prog = *argv;
  op   = DEFAULT_OP;
  pin  = DEFAULT_PIN;
  cmd  = DEFAULT_CMD;

  static struct option long_opts[] = {
    {"on",      no_argument,        NULL, 'o'},
    {"off",     no_argument,        NULL, 'f'},
    {"toggle",  no_argument,        NULL, 't'},
    {"check",   no_argument,        NULL, 'c'},
    {"pin",     required_argument,  NULL, 'p'},
    {"port",    required_argument,  NULL, 'r'},
    {"version", no_argument,        NULL, 'v'},
    {"help",    no_argument,        NULL, 'h'},
    {NULL,      0,                  0,      0}
  };

  while((c = getopt_long(argc, argv, "oftcp:r:vh", long_opts, &opt_index)) != -1) {
    switch(c) {
      // On
      case 'o':
        cmd = '1';
        break;
      // Off
      case 'f':
        cmd = '0';
        break;
      // Toggle
      case 't':
        cmd = 't';
        break;
      // Check
      case 'c':
        op = 'g';
        break;
      // Pin
      case 'p':
        pin = atoi(optarg) + 48;
        break;
      // Port
      case 'r':
        strncpy(port, optarg, MAX_PORT_LEN-1);
        port[MAX_PORT_LEN-1] = '\0';
        break;
      // Version
      case 'v':
        print_version();
        return 0;
      // Help
      case 'h':
      case '?':
      default:
        print_help();
        return (c == 'h') ? 0 : 1;
    }
  }

  if(argc == optind+1) {
    host = argv[optind];
  } else {
    fprintf(stderr, "%s: No host specified. Exiting.\n", prog);
    return 1;
  }

  // Connect to the server
  if((socket = connect_to_server(host, port, server_info)) == FAILURE) {
    fprintf(stderr, "%s: Failed to connect to host: %s\n", prog, strerror(errno));
    return 1;
  }

  // Construct the string to send to the server
  if(op == 'g') {
    sprintf(message, "g\n");
  } else {
    sprintf(message, "s-%c-%c\n", pin, cmd);
  }

  // Send the op to the server
  if(send(socket, message, strlen(message), 0) == FAILURE) {
    fprintf(stderr, "%s: Error sending data to server: %s\n", prog, strerror(errno));
    close(socket);
    return 1;
  }

  // Listen for the response
  if((recv_len = recv(socket, reply, BUFFER, 0)) == FAILURE) {
    fprintf(stderr, "%s: Error receiving data from server: %s\n", prog, strerror(errno));
    close(socket);
    return 1;
  }
  reply[recv_len] = '\0';

  // If a check op, format the reply nicely
  if(op == 'g') {
    print_get_op(reply);
  } else {
    printf("%s: Reply from server: %s\n", prog, reply);
  }

  // Disconnect from the server
  close(socket);

  freeaddrinfo(server_info);

  return 0;
}

int connect_to_server(const char* host, const char *port, struct addrinfo *server_info) {
  int sock;
  struct addrinfo *info_iter;
  struct addrinfo hints;

  // Make sure hints memory is clear
  memset(&hints, 0, sizeof hints);

  hints.ai_family   = AF_UNSPEC;   // IPv4 or IPv6
  hints.ai_socktype = SOCK_STREAM; // Always use sock stream

  // Get the list of address info for the given host
  int ret;
  if((ret = getaddrinfo(host, port, &hints, &server_info)) != SUCCESS) {
    fprintf(stderr, "%s: Failed to get address info. Error: %d\n", prog, ret);
    return FAILURE;
  }

  // Traverse list of results and bind to first socket possible
  for(info_iter=server_info; info_iter!=NULL; info_iter=info_iter->ai_next) {
    // Try to get socket
    if((sock = socket(info_iter->ai_family, info_iter->ai_socktype, info_iter->ai_protocol)) == FAILURE) {
      continue;
    }

    // Try to connect to server
    if(connect(sock, info_iter->ai_addr, info_iter->ai_addrlen) == FAILURE) {
      // Close the opened socket
      close(sock);
      continue;
    }

    // No problems above? We're all set up. Move on!
    break;
  }

  // If info_iter is null, we failed to bind
  if(info_iter == NULL) {
    return FAILURE;
  }

  return sock;
}

void print_get_op(char *reply) {
  // Check for an error
  if(strcmp(reply, "ERR") == 0) {
    printf("%s: Reply from server: %s\n", prog, reply);
  } else {
    int i=0;

    if(reply[i] != '\0' && reply[i+1] != '\0' && reply[i+2] != '\0') {
      do {
        printf("Pin %c: %s\n", reply[i], (reply[i+2] == '1') ? "ON" : "OFF");
        i+=4;
      } while(reply[i] != '\0');
    }
  }
}

void print_help() {
  printf("Usage: %s [options] [host]\n\
    \t--on      (-o)\t\t\tTurn the relay on\n\
    \t--off     (-f)\t\t\tTurn the relay off\n\
    \t--toggle  (-t)\t\t\tToggle relay state\n\
    \t--check   (-c)\t\t\tCheck relay states\n\
    \t--pin     (-p) [pin_num]\tRelay pin to use\n\
    \t--port    (-r) [port_num]\tPort to connect to. Defaults to %s\n\
    \t--version (-v)\t\t\tDisplay the version number\n\
    \t--help    (-h)\t\t\tDisplay this message\n", prog, DEFAULT_PORT);
}

void print_version() {
  printf("%s version %s\n", prog, VERSION);
}
