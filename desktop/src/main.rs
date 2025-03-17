use reqwest;
use serde_json::Value;

fn main() {
  let args = parse_arguments();

  let server = args.get_one::<String>("server").unwrap();
  let port = args.get_one::<String>("port").unwrap();
  let host = format!("{server}{}{port}", if port == "" {""} else {":"});

  let operation = if args.get_flag("on") {
    "on"
  } else if args.get_flag("off") {
    "off"
  } else if args.get_flag("toggle") {
    "toggle"
  } else if args.get_flag("list") {
    "list"
  } else {
    return;
  };

  if operation == "list" {
    print_relay_state(&host);
  } else {
    let pin = args.get_one::<String>("pin").unwrap();
    update_relay(&host, operation, pin);
  }
}

fn update_relay(host: &str, state: &str, pin: &str) {
  match http_post(&format!("http://{host}/api/pins/{pin}"), &format!("state={state}")) {
    Ok(_response) => {},
    Err(error) => eprintln!("Error communicating with relay: {error}"),
  }
}

fn print_relay_state(host: &str) {
  let response = match http_get(&format!("http://{host}/api/pins")) {
    Ok(response) => response,
    Err(error) => {
      eprintln!("Error communicating with relay: {error}");
      return;
    }
  };

  let json = match response.json::<Value>() {
    Ok(json) => json,
    Err(error) => {
      eprintln!("Failed to parse JSON response: {error}");
      return;
    }
  };

  if let Some(map) = json.as_object() {
    for (pin, state) in map {
      println!("Pin {pin}: {state}");
    }
  }
}

fn http_get(url: &str) -> Result<reqwest::blocking::Response, reqwest::Error> {
  let response = reqwest::blocking::Client::new().get(url).send()?;

  if response.status().is_success() {
    return Ok(response);
  } else {
    return Err(response.error_for_status().unwrap_err());
  }
}

fn http_post(url: &str, body: &str) -> Result<reqwest::blocking::Response, reqwest::Error> {
  // The response to setting a pin is a redirect which we don't need to follow
  let client = reqwest::blocking::Client::builder().redirect(reqwest::redirect::Policy::none()).build()?;
  let response = client.post(url).body(String::from(body)).send()?;

  if response.status().is_redirection() {
    return Ok(response);
  } else {
    return Err(response.error_for_status().unwrap_err());
  }
}

fn parse_arguments() -> clap::ArgMatches {
  return clap::Command::new("relay-remote")
    .arg(clap::Arg::new("server")
      .short('s')
      .long("server")
      .help("Relay/server to connect to"))
    .arg(clap::Arg::new("port")
      .short('r')
      .long("port")
      .help("Relay port to connect to")
      .default_value(""))
    .arg(clap::Arg::new("pin")
      .short('p')
      .long("pin")
      .help("Relay pin to use")
      .default_value("9"))
    .arg(clap::Arg::new("on")
      .short('o')
      .long("on")
      .action(clap::ArgAction::SetTrue)
      .help("Turn the relay on"))
    .arg(clap::Arg::new("off")
      .short('f')
      .long("off")
      .action(clap::ArgAction::SetTrue)
      .help("Turn the relay off"))
    .arg(clap::Arg::new("toggle")
      .short('t')
      .long("toggle")
      .action(clap::ArgAction::SetTrue)
      .help("Toggle the relay state"))
    .arg(clap::Arg::new("list")
      .short('l')
      .long("list")
      .action(clap::ArgAction::SetTrue)
      .help("List relay state"))
    .group(clap::ArgGroup::new("operation")
      .args(&["on", "off", "toggle", "list"])
      .required(true))
    .get_matches();
}
