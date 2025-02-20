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
| `-v, --verbose`                            | Specify multiple `-v` options to increase verbosity. For example: `-v -v -v` or `-vvv`. More precise logging can be configured using a logback configuration file. See [How to activate more log?](#how-to-activate-more-log) for details. |
| `-u, --server-url`                         | Set the server URL. Defaults to `-u coap://localhost:5685` or `-u coaps://localhost:5686`.                                                                                                                                                 |
| `-b, --bootstrap`                          | Use bootstrap mode instead of direct registration.                                                                                                                                                                                         |
| `-n, --endpoint-name`                      | Set the endpoint name for the client. Default: `-n ${hostname}` or `-n ThingsboardLwm2mClientDemo`.                                                                                                                                        |
| `-l, --lifetime`                           | Registration lifetime in seconds (default: `-l 300` in sec).                                                                                                                                                                               |
| `-cp, --communication-period`              | Period for client-server communication (should be smaller than lifetime). It will be used even if -b is used.                                                                                                                              |
| `-q, --queue-mode`                         | Enable queue mode (not fully implemented).                                                                                                                                                                                                 |
| `-m, --models-folder`                      | Path to a folder containing OMA DDF (XML) object models. [See Use object models from a custom folder:](#use-object-models-from-a-custom-folder)                                                                                            |
| `-o, --ota-folder`                         | Path to a folder containing OMA DDF (XML) object models. [See Use OTA from a custom folder:](#use-ota-from-a-custom-folder)                                                                                                                |
| `-t, --test-objects`                       | Enables testing of custom-programmed algorithms (like OTA). Test mode is available for Object IDs 5, 9, and 19.  Syntax example: `-t`.                                                                                                     |
| `-aa, --additional-attributes`             | Additional attributes to send during registration. For example: `-aa attr1=value1,attr2=value2`.                                                                                                                                           |
| `-bsaa, --bootstrap-additional-attributes` | Additional attributes for bootstrap. Syntax example: `-bsaa attr1=value1,attr2=value2`.                                                                                                                                                    |
| `-ocf, --support-old-format`               | Enable support for old/unofficial content formats. Syntax example: `-ocf`. See [Leshan support old TLV and JSON code](https://github.com/eclipse/leshan/pull/720).                                                                         |
| `-jc, --use-java-coap`                     | Use Java-CoAP instead of Californium. Syntax example: `-jc`.                                                                                                                                                                               |


### Use object models from a custom folder:

```sh
java -jar thingsboard-lw-demo-client.jar -m ./
java -jar thingsboard-lw-demo-client.jar -m ./models
java -jar thingsboard-lw-demo-client.jar -m /absolute_path/models
```

### Use ota from a custom folder:

```sh
java -jar thingsboard-lw-demo-client.jar -o ./
java -jar thingsboard-lw-demo-client.jar -o ./ota
java -jar thingsboard-lw-demo-client.jar -o /absolute_path/ota
```

[OTA  firmware and software update](https://thingsboard.io/docs/user-guide/ota-updates).

Example file settings current FW for ThingsBoard LwM2M Demo Client (format json), according to the specified location  `-o ./ota:`

_Example update FW (`./ota/OtaFW.json`):_
* Type: `fw`
* Title: `fw_test`
* Version: `1.1`
* Tag: `fw_test 1.1`
* FilePath: `./ota`
* File: `otaPackageFW.bin`
* Checksum (SHA256): `07385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf800`,
* File Size: `8283052` in bytes

```json5
{
    "type": "fw",
    "title": "fw_test",
    "version": "1.1",
    "file": "otaPackageFW.bin",
    "checksumSHA256": "07385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf800",
    "fileSize": 8283052    
}
```

_Example update SW (`./ota/OtaSW.json`):_
* Type: `sw`
* Title: `sw_test`
* Version: `1.5`
* Tag: `sw_test 1.5`
* FilePath: `./ota`
* File: `otaPackageSW.bin`
* Checksum (SHA256): `12385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf8cd`,
* File Size: `9283056` in bytes

```json5
{
    "type": "sw",
    "title": "sw_test",
    "version": "1.5",
    "file": "otaPackageSW.bin",
    "checksumSHA256": "12385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf8cd",
    "fileSize": 9283056    
}
```

```sh
java -jar thingsboard-lw-demo-client.jar -o ./ota
```

## Location Options

| Option                     | Description                                                                                                                                      |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `-pos, --initial-position` | Set the device's initial location (latitude, longitude). Format: `-pos lat:long` (default: random). Syntax example: `-pos 34.122222:118.4111111` |
| `-sf, --scale-factor`      | Scaling factor for position updates (default: `1.0`). Syntax example: `-sf 3.0`.                                                                 |

### pos

In the **ThingsBoard LwM2M Demo Client**, _atitude_ and _longitude_ values are adjusted to ensure they remain in the positive range. This logic converts the traditional latitude/longitude format (which includes negative values) into a fully positive coordinate system.

#### Explanation of each case:
When the application is started, it is created in LeshanClient:

*I.* If `-pos lat:long`, i.e. _latitude_ and _longitude_ are not null
1. _latitude_ + `90f`
* Latitude values typically range from -90 to +90 degrees.
* Adding 90f shifts this range to [0, 180], possibly to avoid negative values and simplify storage.

```markdown
`-pos 34.122222:118.4111111`
this.latitude = `latitude` + `90f`;
this.latitude = `124.122222`;
this.scaleFactor = `scaleFactor`;
this.scaleFactor = `1.0`;
```

2. _longitude_ + `180f`
* Longitude values usually range from -180 to +180 degrees.
* Adding 180f shifts this range to [0, 360], ensuring only positive values are stored.

```markdown
`-pos 34.122222:118.4111111`
this.longitude = `longitude` + `180f`;
this.longitude = `298.4111111`;
this.scaleFactor = `scaleFactor`;
this.scaleFactor = `1.0`;
```

*II.* if `-pos lat:long` is absent, i.e. _latitude_ and _longitude_ are  *`null`*
1. RANDOM.nextInt(180) for latitude
* a random value between 0 and 179 is assigned, keeping it within the adjusted [0, 180] range.
* 
```markdown
`-sf 3.0`
this.scaleFactor = `scaleFactor`;
this.scaleFactor = `3.0`;
this.latitude = RANDOM.nextInt(`180`);
this.latitude = `20.0`;
```

2. RANDOM.nextInt(360) for longitude
* a random value between 0 and 359 is generated, matching the adjusted [0, 360] range.

```markdown
`-sf 3.0`
this.scaleFactor = `scaleFactor`;
this.scaleFactor = `3.0`;
this.longitude = RANDOM.nextInt(`360`);
this.longitude = `140.0`;
```

### sf

```markdown
`-sf 3.0`
this.scaleFactor = `scaleFactor`;
this.scaleFactor = `3.0`;
this.latitude = RANDOM.nextInt(`180`);
this.latitude = `20.0`;
this.longitude = RANDOM.nextInt(`360`);
this.longitude = `140.0`;
```

```markdown
`-pos 34.122222:118.4111111 -sf 2.0`
this.scaleFactor = `scaleFactor`;
this.scaleFactor = `3.0`;
this.latitude = `latitude` + `90f`;
this.latitude = `124.122222`;
this.longitude = `longitude` + `180f`;
this.longitude = `298.4111111`;
```

## DTLS (Security) Options

| Option                              | Description                                                                                                                                                                                                                                                                                                      |
|-------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-r, --rehanshake-on-update`        | Force rehandshake on registration update. Syntax example: `r`.                                                                                                                                                                                                                                                   |
| `-f, --force-full-handshake`        | By default client will try to resume DTLS session by using abbreviated Handshake. This option force to always do a full handshake. Syntax example: `f`.                                                                                                                                                          |
| `-cid, --connection-id`             | Enable DTLS connection ID (default: off). Control usage of DTLS connection ID: - 'on' to activate Connection ID support (same as -cid 0); - 'off' to deactivate it; - Positive value define the size in byte of CID generated;  0 value means we accept to use CID but will not generated one for foreign peer." |
| `-c, --cipher-suites`               | List of cipher suites to use (comma-separated). Define cipher suites to use. CipherCuite enum value separated by ',' without spaces. E.g: TLS_PSK_WITH_AES_128_CCM_8,TLS_PSK_WITH_AES_128_CCM.                                                                                                                   |
| `-oc, --support-deprecated-ciphers` | Enable support for deprecated cipher suites. Syntax example: `-oc`.                                                                                                                                                                                                                                              |

### Example Commands
#### Cipher suites to use


```sh
java -jar thingsboard-lw-demo-client.jar -u coap://demo.thingsboard.io -n MyClientNoSec -c TLS_PSK_WITH_AES_128_CCM_8,TLS_PSK_WITH_AES_128_CCM
```

#### DTLS Connection ID (CID) support

* - 'on' to activate Connection ID support (same as `-cid 0`). `0` value means we accept to use CID but will not generated one for foreign peer.

```sh
java -jar thingsboard-lw-demo-client.jar -u coaps://demo.thingsboard.io -n MyClientPsk --psk-identity myIdentity --psk-key mySecret -cid 0
```

_What is **`-cid`**?_

The -cid (Connection ID) option enables DTLS Connection ID (CID) support.

CID is used in DTLS 1.2 and 1.3 to maintain secure communication sessions even when the underlying transport (e.g., UDP) changes.

_Possible Values for **`-cid`**_:

* Any positive integer (`cid > 0`) is valid.
* The value typically represents the CID length (`number of bytes`).
* Common values: `1, 2, 4, 8, 16` (depends on DTLS implementation).

_What Does **`-cid`** Affect?_

* Maintains DTLS session continuity. Normally, DTLS relies on IP+Port for session tracking. If a device changes network (e.g., mobile IP change), CID allows the session to persist.
* Reduces DTLS handshake overhead. Without CID, losing connection means a full DTLS handshake is required again. With CID, the session is resumed, saving time and resources.
* Security Considerations. A longer CID value increases uniqueness but adds packet overhead. Too short CID values (e.g., 1) might increase the risk of collisions.

```sh
java -jar thingsboard-lw-demo-client.jar -u coaps://demo.thingsboard.io -n MyClientPsk --psk-identity myIdentity --psk-key mySecret -cid 4
```

#### Register with the ThingsBoard server (mode NoSec):

```sh
java -jar thingsboard-lw-demo-client.jar -u coap://demo.thingsboard.io -n MyClientNoSec
```

#### Register with the ThingsBoard server (mode with DTLS):

| Option                         | Description                                                                                                                                                                                                                                                                                                                                                                                                                         |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-i, --psk-identity`           | Set the LWM2M or Bootstrap server PSK identity in ascii.                                                                                                                                                                                                                                                                                                                                                                            |
| `-p, --psk-key`                | Set the LWM2M or Bootstrap server Pre-Shared-Key in hexa.                                                                                                                                                                                                                                                                                                                                                                           |
| `-cprik, --client-private-key` | The path to your client private key file, The private key should be in PKCS#8 format (DER encoding).                                                                                                                                                                                                                                                                                                                                |
| `-cpubk, --client-public-key`  | The path to your client public key file. The public Key should be in SubjectPublicKeyInfo format (DER encoding).                                                                                                                                                                                                                                                                                                                    |
| `-spubk, --server-public-key`  | "The path to your server public key file. The public Key should be in SubjectPublicKeyInfo format (DER encoding).                                                                                                                                                                                                                                                                                                                   |
| `-ccert, --client-certificate` | The path to your client certificate file.", The certificate Common Name (CN) should generaly be equal to the client endpoint name (see -n option).", The certificate should be in X509v3 format (DER encoding).                                                                                                                                                                                                                     |
| `-scert, --server-certificate` | The path to your server certificate file (see -certificate-usage option). The certificate should be in X509v3 format (DER encoding).                                                                                                                                                                                                                                                                                                |
| `-cu, --certificate-usage`     | Certificate Usage (as integer) defining how to use server certificate",  - 0 : CA constraint";  - 1 : service certificate constraint; - 2 : trust anchor assertion"; - 3 : domain issued certificate (Default value) [Usage are described at](https://tools.ietf.org/html/rfc6698#section-2.1.1)                                                                                                                                    |
| `-ts, --truststore`            | The path to  : ", - a root certificate file to trust, - OR a folder containing trusted certificates, - OR trust store URI; Certificates must be in in X509v3 format (DER encoding); URI format:  file://<path-to-store>#<password>#<alias-pattern> Where :  - path-to-store is path to pkcs12 trust store file, - alias-pattern can be used to filter trusted certificates and can also be empty to get all,  Default: empty store. |

##### Use DTLS with PSK authentication:

```sh
java -jar thingsboard-lw-demo-client.jar -u coaps://demo.thingsboard.io -n MyClientPsk --psk-identity myIdentity --psk-key mySecret
```

##### Use DTLS with RPK authentication:

Use CoAP over DTLS with Raw Public Key, -cpubk -cprik -spubk options should be used together. [RPK](https://github.com/eclipse/leshan/wiki/Credential-files-format)

```sh
java -jar thingsboard-lw-demo-client.jar -u coaps://demo.thingsboard.io -n MyClientRpk -cpubk ./clietPubK.der -cprik ./clientKey.der -spubk ./serverPubK.der
```

##### Use DTLS with X509 authentication:

```sh
java -jar thingsboard-lw-demo-client.jar -u coaps://demo.thingsboard.io -n MyClientX509 -ccert ./clientX509v3.der -scert ./serverX509v3.der (optional)-cu 2 
```

## Notes

- Ensure the server URL includes the correct scheme (`coap://` or `coaps://`).
- When using DTLS, specify authentication credentials (PSK, RPK, or X.509).
- Custom LwM2M object models should be in OMA DDF format and placed in the specified directory.

For further details, refer to the [ThingsBoard Documentation](https://thingsboard.io/docs/).

## How to activate more log?

This program uses [SLF4J](https://en.wikipedia.org/wiki/SLF4J) as a logging facade, meaning that you can use any compatible backend for your application.

Our demos use the [logback](https://logback.qos.ch/) backend. A verbosity option (`-v, -vv`, ...) allows changing the log level.

To activate more logs for these demos, see [More logs on ThingsBoard LwM2M Demo Client](#more-logs-on-thingsboard-lw-demo-client) or read the [logback documentation](https://logback.qos.ch/manual/configuration.html).

### More logs on ThingsBoard LwM2M Demo Client

#### After start with options to increase verbosity. For example, `-v` or `-vv` or `-vvv`

```sh
java -jar thingsboard-lw-demo-client.jar -u coap://demo.thingsboard.io -n MyClient -v
```

**Note:** Depending on the number of `v` elements, the logging level for the _"org.eclipse.leshan", "org.eclipse.californium"_ classes is set:

* `-v`    - INFO
* `-vv`   - DEBUG
* `-vvv`  - TRACE
* `-vvvv` - TRACE + Logger.ROOT_LOGGER_NAME (TRACE)

#### You could also try to activate more logs on the hingsBoard LwM2M Demo Client by adding this to your command line:

```sh
-Dlogback.configurationFile="path_to_your_logback_config.xml"
```

Example:

```sh
java -Dlogback.configurationFile="logback-config.xml" -jar target/thingsboard-lw-demo-client-{version}.jar
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

### Enables you to execute some dynamic commands from the Interactive Console.

| Commands: | Description                                                                                                                                                                                                          |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `help`    | Display help information about the specified command.                                                                                                                                                                |
| `list`    | List available Objects, Instances and Resources. For example: `list` or `list 5` for ObjectId.                                                                                                                       |
| `create`  | Enable a new Object. Format: `create ${ObjectId}`. For example: `create 5`, the Object with ID = `5` is created with the latest available version, `1.2`. or `create 5 1.1` created ObjectId = `5` with ver = `1.1`. |
| `delete`  | Desable a Object. . Format: `delete ${ObjectId}`. For example: `delete 5`, the Object with ID = `5` is desabled.                                                                                                     |
| `send`    | Send data to server.                                                                                                                                                                                                 |
| `collect` | Collect data to send it later with 'send' command                                                                                                                                                                    |
| `move`    | Simulate client mouvement.                                                                                                                                                                                           |
| `update`  | Trigger a registration update.                                                                                                                                                                                       |
| `reboot`  | Restart client without update object.                                                                                                                                                                                |

#### move

* `-d` or `north` - Move to the North, For example: `move -d`, result in objectId = 6 (MyLocation): _latitude_ = **latitude** + `1.0f` * **scaleFactor**;
* `-e` or `east` - Move to the East, For example: `move -e`, result in objectId = 6 (MyLocation): _longitude_ = **longitude** + `-1.0f` * **scaleFactor**;
* `-s` or `south` Move to the South, For example: `move -s`, result in objectId = 6 (MyLocation): _latitude_ = **latitude** + `-1.0f` * **scaleFactor**;
* `-w` or `west` Move to the West, For example: `move -w`, result in objectId = 6 (MyLocation): _longitude_ = **longitude** + `1.0f` * **scaleFactor**;

#### send

Explanation:

**send** → Sends data to the server.
*/3303/0/5700*=`25.3` → Specifies the LwM2M resource to update:

| Params: | Description                           |
|---------|---------------------------------------|
| `3303`  | Object ID (Temperature Sensor).       |
| `0`     | Instance ID.                          |
| `5700`  | Resource ID (Sensor Value).           |
| `25.3`  | New value for the resource.           |


```sh
send `/3303/0/5700=25.3`
```

This updates multiple resources at once:

| Params:              | Description                                            |
|----------------------|--------------------------------------------------------|
| `/3323/1/5601=10.5`  | Updates Min Value for Object 3323 (Power Measurement). |
| `/3323/1/5602=50.8`  | Updates Max Value.                                     |


```sh
send /3323/1/5601=10.5 /3323/1/5602=50.8
```

#### collect

After collecting the data, use the send command to transmit all stored values.

Explanation:

**collect** → Stores the specified value without immediately sending it to the server.

*/3303/0/5700*=`22.5` → Specifies the LwM2M resource to collect:

| Params: | Description                           |
|---------|---------------------------------------|
| `3303`  | Object ID (Temperature Sensor).       |
| `0`     | Instance ID.                          |
| `5700`  | Resource ID (Sensor Value).           |
| `22.5`  | New value for the resource.           |


```sh
collect /3303/0/5700=22.5
send
```

```sh
collect /3323/1/5601=15.7
collect /3323/1/5602=48.2
send
```
**This sequence:**
* _Collects_ `15.7` as the Min Value for Power Measurement.
* _Collects_ `48.2` as the Max Value for Power Measurement.
* _Sends_ `all` collected `values at once`.
