# ThingsBoard LwM2M Demo Client

## Overview

The **ThingsBoard LwM2M Demo Client** is a command-line tool for simulating an LwM2M client and connecting it to a ThingsBoard server. It supports various configuration options, including server connection settings, security options (DTLS), and object model customization.

## Usage

To run the client, use the following command:

```sh
java -jar thingsboard-lw-demo-client.jar [options]
```

## General Options

| Option                                     | Description                                                                                                                                                                                                                                |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-h, --help`                               | Display help information.                                                                                                                                                                                                                  |
| `-V, --version`                            | Print version information and exit.                                                                                                                                                                                                        |
| `-v, --verbose`                            | Specify multiple `-v` options to increase verbosity. For example, `-v -v -v` or `-vvv`. More precise logging can be configured using a logback configuration file. See [How to activate more log?](#how-to-activate-more-log) for details. |
| `-u, --server-url`                         | Set the server URL. Defaults to `-u coap://localhost:5685` or `-u coaps://localhost:5686`.                                                                                                                                                 |
| `-b, --bootstrap`                          | Use bootstrap mode instead of direct registration.                                                                                                                                                                                         |
| `-n, --endpoint-name`                      | Set the endpoint name for the client. Default: `-n ${hostname}` or `-n ThingsboardLwm2mClientDemo`.                                                                                                                                        |
| `-l, --lifetime`                           | Registration lifetime in seconds (default: `-l 300` in sec).                                                                                                                                                                               |
| `-cp, --communication-period`              | Period for client-server communication (should be smaller than lifetime). It will be used even if -b is used.                                                                                                                              |
| `-q, --queue-mode`                         | Enable queue mode (not fully implemented).                                                                                                                                                                                                 |
| `-m, --models-folder`                      | Path to a folder containing OMA DDF (XML) object models. For example: `-m /.` or `-m /models`or `-m /absolute_path`                                                                                                                        |
| `-t, --test-objects`                       | Enables testing of custom-programmed algorithms (like OTA). Test mode is available for Object IDs 5, 9, and 19.  Syntax example: `-t`.                                                                                                     |
| `-aa, --additional-attributes`             | Additional attributes to send during registration (e.g., `-aa attr1=value1,attr2=value2`).                                                                                                                                                 |
| `-bsaa, --bootstrap-additional-attributes` | Additional attributes for bootstrap (same syntax as `-aa`).                                                                                                                                                                                |
| `-ocf, --support-old-format`               | Enable support for old/unofficial content formats.                                                                                                                                                                                         |
| `-jc, --use-java-coap`                     | Use Java-CoAP instead of Californium.                                                                                                                                                                                                      |

## Location Options

| Option                     | Description                                                                                    |
| -------------------------- | ---------------------------------------------------------------------------------------------- |
| `-pos, --initial-position` | Set the device's initial location (latitude, longitude). Format: `lat:long` (default: random). |
| `-sf, --scale-factor`      | Scaling factor for position updates (default: 1.0).                                            |

## DTLS (Security) Options

| Option                              | Description                                     |
| ----------------------------------- | ----------------------------------------------- |
| `-r, --rehanshake-on-update`        | Force rehandshake on registration update.       |
| `-f, --force-full-handshake`        | Always perform a full DTLS handshake.           |
| `-cid, --connection-id`             | Enable DTLS connection ID (default: off).       |
| `-c, --cipher-suites`               | List of cipher suites to use (comma-separated). |
| `-oc, --support-deprecated-ciphers` | Enable support for deprecated cipher suites.    |

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
java -jar thingsboard-lw-demo-client.jar -m ./
java -jar thingsboard-lw-demo-client.jar -m ./models
java -jar thingsboard-lw-demo-client.jar -m /absolute_path
```

## Notes

- Ensure the server URL includes the correct scheme (`coap://` or `coaps://`).
- When using DTLS, specify authentication credentials (PSK, RPK, or X.509).
- Custom LwM2M object models should be in OMA DDF format and placed in the specified directory.

For further details, refer to the [ThingsBoard Documentation](https://thingsboard.io/docs/).

## How to activate more log?

This program uses [SLF4J](https://en.wikipedia.org/wiki/SLF4J) as a logging facade, meaning that you can use any compatible backend for your application.

Our demos use the [logback](https://logback.qos.ch/) backend. Since 2.0.0-M5, a verbosity option (`-v, -vv`, ...) allows changing the log level.

To activate more logs for these demos, see [More logs on ThingsBoard LwM2M Demo Client](#more-logs-on-thingsboard-lw-demo-client) or read the [logback documentation](https://logback.qos.ch/manual/configuration.html).

### More logs on ThingsBoard LwM2M Demo Client

After start with options to increase verbosity. For example, `-v` or `-vv` or `-vvv` 

```sh
java -jar thingsboard-lw-demo-client.jar -u coap://demo.thingsboard.io -n MyClient -v
```

1. Enables you to execute some dynamic commands from the Interactive Console.

| Commands: | Description                                    |
|-----------| ---------------------------------------------- |
| `help`    | Display help information about the specified command.      |
| `list`    | List available Objects, Instances and Resources |
| `create`  | Enable a new Object |
| `delete`  |  Send data to server |
| `send`   | Enable support for deprecated cipher suites    |
| `collect`   | Collect data to send it later with 'send' command |
| `move`   | Simulate client mouvement |

2. Depending on the number of "v" elements, the logging level for the "org.eclipse.leshan", "org.eclipse.californium" classes is set:

- "v"    - INFO
- "vv"   - DEBUG
- "vvv"  - TRACE
- "vvvv" - TRACE + Logger.ROOT_LOGGER_NAME (TRACE)

You could also try to activate more logs on the hingsBoard LwM2M Demo Client by adding this to your command line:

```sh
-Dlogback.configurationFile="path_to_your_logback_config.xml"
```

Example:

```sh
java -Dlogback.configurationFile="logback-config.xml" -jar target/lthingsboard-lw-demo-client-{version}.jar
```

And in your logback config:

```xml
<configuration>
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
                <encoder>
                        <pattern>%d %p %C{0} - %m%n</pattern>
                </encoder>
        </appender>

        <root level="WARN">
                <appender-ref ref="STDOUT" />
        </root>

        <logger name="org.eclipse.leshan" level="DEBUG"/><!-- default value is INFO -->
        <logger name="org.eclipse.leshan.server.security.SecurityCheck" level="DEBUG"/>
        <logger name="org.eclipse.leshan.core.model.LwM2mModel" level="TRACE"/>

        <!-- All above is the default config, the line below is to search something in the DTLS stack -->
        <logger name="org.eclipse.californium" level="DEBUG"/>
</configuration>
```

