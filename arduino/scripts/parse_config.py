#!/usr/bin/env python

import functools
import sys
import yaml

TYPE_WIRED_SERVER = 'wired_server'
TYPE_WIFI_SERVER = 'wifi_server'
TYPE_WIFI_CLIENT = 'wifi_client'

def main():
  relay = sys.argv[2]
  config = read_config(sys.argv[1])
  flags = []

  if relay not in config:
    print(f"{relay} not found in {sys.argv[1]}")
    sys.exit(1)

  validate_key(config[relay], 'type')
  type = config[relay]['type']

  # Only servers have UI options
  if type in [TYPE_WIRED_SERVER, TYPE_WIFI_SERVER]:
    parse_ui_keys(config[relay], flags)

  if type == TYPE_WIFI_CLIENT:
    parse_client_keys(config[relay], flags)

  parse_network_keys(config[relay], type, flags)

  print(' '.join(flags))


def parse_ui_keys(config, flags):
  validate_key(config, 'ui', 'name')
  flags.append(f"-DRELAY_NAME='\"{config['ui']['name']}\"'")

  validate_key(config, 'ui', 'pins')
  configured_pins = list(config['ui']['pins'].keys())
  flags.append(f"-DCONFIGURED_PINS='{', '.join(map(str, configured_pins))}'")
  flags.append(f"-DMAX_PIN={max(configured_pins)}")

  for index in range(0, 10):
    label = config['ui']['pins'].get(index, '')
    flags.append(f"-DPIN_{index}_LABEL='\"{label}\"'")


def parse_client_keys(config, flags):
  validate_key(config, 'clients')

  flags.append(f"-DNETWORK_CLIENT_IPS=\"{', '.join([f"\\\"{client['ip']}\\\"" for client in config['clients']])}\"")
  flags.append(f"-DNETWORK_CLIENT_PINS=\"{', '.join([str(client['pin']) for client in config['clients']])}\"")


def parse_network_keys(config, type, flags):
  validate_key(config, 'network', 'ip')
  flags.append(f"-DNETWORK_IP=\"{', '.join(config['network']['ip'].split('.'))}\"")

  if type == TYPE_WIRED_SERVER:
    validate_key(config, 'network', 'mac')
    flags.append(f"-DNETWORK_MAC=\"{', '.join([f"0x{octet}" for octet in config['network']['mac'].split(':')])}\"")

  validate_key(config, 'network', 'gateway')
  flags.append(f"-DNETWORK_GATEWAY=\"{', '.join(config['network']['gateway'].split('.'))}\"")

  validate_key(config, 'network', 'subnet')
  flags.append(f"-DNETWORK_SUBNET=\"{', '.join(config['network']['subnet'].split('.'))}\"")

  if type in [TYPE_WIFI_SERVER, TYPE_WIFI_CLIENT]:
    validate_key(config, 'network', 'ssid')
    flags.append(f"-DNETWORK_SSID='\"{config['network']['ssid']}\"'")

    validate_key(config, 'network', 'passphrase')
    flags.append(f"-DNETWORK_PASSPHRASE='\"{config['network']['passphrase']}\"'")


def read_config(path):
  with open(path, 'r') as file:
    return yaml.safe_load(file)


def validate_key(config, *keys):
  if not dig(config, *keys):
    print(f"Missing keys {'->'.join(keys)} from relay configuration.")
    sys.exit(1)


def dig(dictionary, *keys):
  return functools.reduce(lambda current, key: current.get(key) if isinstance(current, dict) else None, keys, dictionary)

if __name__ == "__main__":
  main()
