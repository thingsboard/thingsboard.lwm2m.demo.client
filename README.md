# ThingsBoard LwM2M Demo Client

## Overview

The **ThingsBoard LwM2M Demo Client** is a command-line tool for simulating an LwM2M client and connecting it to a ThingsBoard server. It supports various configuration options, including server connection settings, security options (DTLS), and object model customization.

## Usage

To run the client, use the following command:

```sh
java -jar thingsboard-lw-demo-client.jar [options]
```

## General Options

| Option                                     | Description                                                                                |
| ------------------------------------------ | ------------------------------------------------------------------------------------------ |
| `-h, --help`                               | Display help information.                                                                  |
| `-V, --version`                            | Print version information and exit.                                                        |
| `-v, --verbose`                            | Specify multiple `-v` options to increase verbosity. For example, `-v -v -v` or `-vvv`. More precise logging can be configured using a logback configuration file. See [How to activate more log?](#how-to-activate-more-log) for details. |
| `-u, --server-url`                         | Set the server URL. Defaults to `coap://localhost:5685` or `coaps://localhost:5686`.       |
| `-b, --bootstrap`                          | Use bootstrap mode instead of direct registration.                                         |
| `-n, --endpoint-name`                      | Set the endpoint name for the client. Default: hostname or `LeshanClientDemo`.             |
| `-l, --lifetime`                           | Registration lifetime in seconds (default: 300s).                                          |
| `-cp, --communication-period`              | Period for client-server communication (should be smaller than lifetime).                  |
| `-q, --queue-mode`                         | Enable queue mode (not fully implemented).                                                 |
| `-m, --models-folder`                      | Path to a folder containing OMA DDF (XML) object models.                                   |
| `-t, --test-objects=<objectForTest>`       | Specifies a positive integer flag for testing custom-programmed algorithms. Must be > 0. Testing custom-programmed algorithms is restricted to Object IDs 5, 9, and 19. Default: 1. Syntax: `-t 1`. |
| `-aa, --additional-attributes`             | Additional attributes to send during registration (e.g., `-aa attr1=value1,attr2=value2`). |
| `-bsaa, --bootstrap-additional-attributes` | Additional attributes for bootstrap (same syntax as `-aa`).                                |
| `-ocf, --support-old-format`               | Enable support for old/unofficial content formats.                                         |
| `-jc, --use-java-coap`                     | Use Java-CoAP instead of Californium.                                                      |

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
java -jar thingsboard-lw-demo-client.jar -m ./models
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

