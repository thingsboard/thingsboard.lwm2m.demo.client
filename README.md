# ThingsBoard LwM2M Demo Client

## Overview
The **ThingsBoard LwM2M Demo Client** is a command-line tool for simulating an LwM2M client and connecting it to a ThingsBoard server. It supports various configuration options, including server connection settings, security options (DTLS), and object model customization.

## Usage
To run the client, use the following command:
```sh
java -jar thingsboard-lw-demo-client.jar [options]
```

## General Options
| Option | Description |
|--------|-------------|
| `-u, --server-url` | Set the server URL. Defaults to `coap://localhost:5685` or `coaps://localhost:5686`. |
| `-b, --bootstrap` | Use bootstrap mode instead of direct registration. |
| `-n, --endpoint-name` | Set the endpoint name for the client. Default: hostname or `LeshanClientDemo`. |
| `-l, --lifetime` | Registration lifetime in seconds (default: 300s). |
| `-cp, --communication-period` | Period for client-server communication (should be smaller than lifetime). |
| `-q, --queue-mode` | Enable queue mode (not fully implemented). |
| `-m, --models-folder` | Path to a folder containing OMA DDF (XML) object models. |
| `-aa, --additional-attributes` | Additional attributes to send during registration (e.g., `-aa attr1=value1,attr2=value2`). |
| `-bsaa, --bootstrap-additional-attributes` | Additional attributes for bootstrap (same syntax as `-aa`). |
| `-ocf, --support-old-format` | Enable support for old/unofficial content formats. |
| `-jc, --use-java-coap` | Use Java-CoAP instead of Californium. |

## Location Options
| Option | Description |
|--------|-------------|
| `-pos, --initial-position` | Set the device's initial location (latitude, longitude). Format: `lat:long` (default: random). |
| `-sf, --scale-factor` | Scaling factor for position updates (default: 1.0). |

## DTLS (Security) Options
| Option | Description |
|--------|-------------|
| `-r, --rehanshake-on-update` | Force rehandshake on registration update. |
| `-f, --force-full-handshake` | Always perform a full DTLS handshake. |
| `-cid, --connection-id` | Enable DTLS connection ID (default: off). |
| `-c, --cipher-suites` | List of cipher suites to use (comma-separated). |
| `-oc, --support-deprecated-ciphers` | Enable support for deprecated cipher suites. |

## Example Commands
### Register with the ThingsBoard server:
```sh
java -jar thingsboard-lw-demo-client.jar -u coap://demo.thingsboard.io -n MyClient
```

### Use DTLS with PSK authentication:
```sh
java -jar thingsboard-lw-demo-client.jar -u coaps://demo.thingsboard.io -n MyClient --psk-identity myIdentity --psk-key mySecret
```

### Use object models from a custom folder:
```sh
java -jar thingsboard-lw-demo-client.jar -m ./models
```

## Notes
- Ensure the server URL includes the correct scheme (`coap://` or `coaps://`).
- When using DTLS, specify authentication credentials (PSK, RPK, or X.509).
- Custom LwM2M object models should be in OMA DDF format and placed in the specified directory.

For further details, refer to the [ThingsBoard Documentation](https://thingsboard.io/docs/).

