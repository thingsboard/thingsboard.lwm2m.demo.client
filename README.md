# ThingsBoard LwM2M Demo Client

## Overview

The **ThingsBoard LwM2M Demo Client** is a command-line tool for simulating an LwM2M client and connecting it to a ThingsBoard server. It supports various configuration options, including server connection settings, security options (DTLS), and object model customization.

## Usage

To run the client, use the following command:

```sh
java -jar thingsboard-lwm2m-demo-client.jar [options]
```

## General Options

| Option                                     | Description                                                                                                                                                                                                                                                                           |
|:-------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-h, --help`                               | Display help information.                                                                                                                                                                                                                                                             |
| `-v`, `-vv`, `-vvv`, `-vvvv`               | Specify multiple `-v` options to increase verbosity. For example: `-v -v -v` or `-vvv`. More precise logging can be configured using a logback configuration file. See [How to activate more log?](#how-to-activate-more-log) for details.                                            |
| `-u, --server-url`                         | Set the server URL. Defaults to `-u coap://localhost:5685` or `-u coaps://localhost:5686`.                                                                                                                                                                                            |
| `-b, --bootstrap`                          | Use bootstrap mode instead of direct registration.                                                                                                                                                                                                                                    |
| `-n, --endpoint-name`                      | Set the endpoint name for the client. Default:`-n ${hostname}` or `-n ThingsboardLwm2mClientDemo`.                                                                                                                                                                                    |
| `-l, --lifetime`                           | Registration lifetime in seconds (default:`-l 300` in sec).                                                                                                                                                                                                                           |
| `-cp, --communication-period`              | Period for client-server communication (should be smaller than lifetime). It will be used even if -b is used.                                                                                                                                                                         |
| `-q, --queue-mode`                         | Enable queue mode (not fully implemented).                                                                                                                                                                                                                                            |
| `-m, --models-folder`                      | Path to a folder containing OMA DDF (XML) object models. See [Use object models from a custom folder:](#use-object-models-from-a-custom-folder)                                                                                                                                       |
| `-o, --ota-folder`                         | Path to the folder containing OTA information for firmware or software. See [Using OTA from a Custom Folder](#using-ota-from-a-custom-folder)                                                                                                                                         |
| `-tobj, --test-objects`                    | Enables testing of custom-programmed algorithms (e.g., OTA).Test mode is available for Object IDs 5, 9.  Syntax example: `-tobj`.                                                                                                                                                     |
| `-tota, --test-ota`                        | Allows testing of firmware and software updates using real OTA files. Test mode supports Object IDs 5 and 9, utilizing Object 19. Using Object 19 (instance 65456 for firmware, 65457 for software) to pass additional OTA file information in JSON format.  Syntax example: `-tota`. |
| `-aa, --additional-attributes`             | Additional attributes to send during registration. For example:`-aa attr1=value1,attr2=value2`.                                                                                                                                                                                       |
| `-bsaa, --bootstrap-additional-attributes` | Additional attributes for bootstrap. Syntax example:`-bsaa attr1=value1,attr2=value2`.                                                                                                                                                                                                |
| `-ocf, --support-old-format`               | Enable support for old/unofficial content formats. Syntax example:`-ocf`. See [Leshan support old TLV and JSON code](https://github.com/eclipse/leshan/pull/720).                                                                                                                     |
| `-jc, --use-java-coap`                     | Use Java-CoAP instead of Californium. Syntax example:`-jc`.                                                                                                                                                                                                                           |
| `-cli, --command-line-interactive`         | Enables interactive command-line mode for executing dynamic commands. Syntax example:`-cli`.                                                                                                                                                                                          |
| `-tcli, --time-out-cli`                    | Timeout interval (in seconds) for flushing logs if no user input is received in CLI mode. Syntax example:`-tcli 10`.                                                                                                                                                                  |

**Note:** Only one of these parameters (`-tobj` or `-tota`) can be used at a time.

### Use object models from a custom folder:

```sh
java -jar thingsboard-lwm2m-demo-client.jar -m ./
java -jar thingsboard-lwm2m-demo-client.jar -m ./models
java -jar thingsboard-lwm2m-demo-client.jar -m /absolute_path/models
```

### Using Test OTA or Test Object

These modes are designed to test different data acquisition algorithms and process information according to real client testing rules.

#### Using Test with `-tobj`

**Large File Limitation:** The file size must not exceed **8192 bytes**.

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -tobj
```

#### Using Test with `-tota`

**Large File Limitation:** The file size must not exceed `256 * 1024 * 1024` bytes (i.e., `268,435,456` bytes).

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -tota
```

#### Using OTA from a Custom Folder

Default value: `-o = ./ota`
Default value of file name for FW: `otaPackageFW.bin`
Default value of file name for SW: `otaPackageSW.bin`

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -o ./
java -jar thingsboard-lwm2m-demo-client-{version}.jar -o ./ota
java -jar thingsboard-lwm2m-demo-client-{version}.jar -o /absolute_path/ota
```

#### Using OTA Updates with ThingsBoard LwM2M Demo Client

[OTA  firmware and software update](https://thingsboard.io/docs/user-guide/ota-updates).

- When OTA Mode is Enabled in the Device Profile -> "Use Resource ObjectId = 19 for OTA updates..."

If the device profile on ThingsBoard is configured with the setting:

"Use Resource ObjectId = 19 for OTA updates:

```
Firmware → InstanceId = 65533

Software → InstanceId = 65534
```

The data format is JSON wrapped in Base64. The main field in JSON is:

```
"Checksum" (SHA256) - Used for integrity validation.

Additional fields:

    "title" - OTA name

    "version" - OTA version

    "fileName" - The name used for storing the OTA on the client

    "dataSize" - OTA size in bytes
```

In this mode, the file settings for current FW in ThingsBoard LwM2M Demo Client (in JSON format) will contain the data sent to ObjectId = 19.

After receiving the OTA file, validation will be performed based on:

```
checksum  (SHA256)

dataSize

The file name will be set according to the value sent in ObjectId = 19.
```

- When OTA Mode is Disabled in the Device Profile -> "Use Resource ObjectId = 19 for OTA updates..."

If the device profile on ThingsBoard is not configured with the setting:
"Use Resource ObjectId = 19..."

```
No validation will be performed after receiving the OTA file.

All actual parameters in file settings for current FW in ThingsBoard LwM2M Demo Client (in JSON format) will be set to default values.
```

- Example file settings current OTA for ThingsBoard LwM2M Demo Client (format json, value default), according to the specified location  `-o ./ota:`

_Example update FW (`./ota/OtaFW.json`):_

* type: `FIRMWARE`
* title: `fw_test`
* version: `1.1`
* filePath: `./ota`
* fileName: `otaPackageFW.bin`
* checksum: `07385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf800`,
* dataSize: `8283052` in bytes

```json5
{
    "_comment": "This JSON file contains firmware metadata.",
    "_commentPackageType": "Always \"fw\" for firmware.",
    "type": "fw",
    "_commentTitle": "The name of the firmware, corresponding to the file fileName.",
    "title": "fw_test",
    "_commentVersione": "The version of the firmware, corresponding to the file fileName.",
    "version": "1.1",
    "_commentFileName": "The name of the firmware file, located in the same directory as this JSON file.",
    "fileName": "otaPackageFW.bin",
    "_commentChecksum": "SHA-256 checksum of the file fileName.",
    "checksum": "07385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf800",
    "_commentDataSize": "Size of the file fileName in bytes.",
    "dataSize": 8283052
}
```

_Example update SW (`./ota/OtaSW.json`):_

* type: `SOFTWARE`
* title: `sw_test`
* version: `1.5`
* filePath: `./ota`
* fileName: `otaPackageSW.bin`
* checksum: `12385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf8cd`,
* dataSize: `9283056` in bytes

```json5
{
    "_comment": "This JSON file contains softmware metadata.",
    "_commentPackageType": "Always \"sw\" for softmware.",
    "type": "sw",
    "_commentTitle": "The name of the softmware, corresponding to the file fileName.",
    "title": "sw_test",
    "_commentVersione": "The version of the softmware, corresponding to the file fileName.",
    "version": "1.1",
    "_commentFileName": "The name of the softmware file, located in the same directory as this JSON file.",
    "fileName": "otaPackageSW.bin",
    "_commentChecksum": "SHA-256 checksum of the file fileName.",
    "checksum": "12385bf4c3c8065987a5eaadd7e6639c28e56e350ed80688df8d497679ebf8cd",
    "_commentDataSize": "Size of the file fileName in bytes.",
    "dataSize": 9283056
}
```

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -o ./ota -tota
```

##### 📦 LwM2M Software Update State Transitions

###### ✅ Successful Software Update Scenario

| Step  | SoftwareUpdateState       | SoftwareUpdateResult                      | Description                                |
|:------|:--------------------------|:------------------------------------------|:-------------------------------------------|
| 1     | `INITIAL (0)`             | `INITIAL (0)`                             | Initial state before any download starts   |
| 2     | `DOWNLOAD_STARTED (1)`    | `DOWNLOADING (1)`                         | Download process has started               |
| 3     | `DOWNLOADED (2)`          | `DOWNLOADING (1)`                         | Package downloaded and integrity verified  |
| 4     | `DELIVERED (3)`           | `SUCCESSFULLY_DOWNLOADED_VERIFIED (3)`    | Package ready to be installed              |
| 5     | `INSTALLED (4)`           | `SOFTWARE_SUCCESSFULLY_INSTALLED (2)`     | Software successfully installed            |
| 6     | `INITIAL (0)`             | `INITIAL (0)`                             | Returned to initial state after Uninstall  |


###### ❌ Invalid Checksum Scenario

| Step  | SoftwareUpdateState       | SoftwareUpdateResult                      | Description                                      |
|:------|:--------------------------|:------------------------------------------|:-------------------------------------------------|
| 1     | `INITIAL (0)`             | `INITIAL (0)`                             | Initial state before download                    |
| 2     | `DOWNLOAD_STARTED (1)`    | `DOWNLOADING (1)`                         | Download has started                             |
| 3     | `DOWNLOADED (2)`          | `PACKAGE_INTEGRITY_CHECK_FAILURE (53)`    | Package download completed, but checksum failed  |
| 4     | `INITIAL (0)`             | `INITIAL (0)`                             | Returned to initial state after failure          |

###### ℹ️ Notes

- `SoftwareUpdateState` defines the current lifecycle phase of the software update process.
- `SoftwareUpdateResult` reflects the latest result of an update attempt.
- The `DELIVERED` state is reached only if the integrity is verified and the package is ready to be installed.
- After a successful `Install` or `Uninstall` operation, the state resets to `INITIAL (0)`.

## Location Options

| Option                        | Description                                                                                                                                          |
|:------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `-pos, --initial-position`    | Set the device's initial location (latitude, longitude). Format:`-pos lat:long` (default: random). Syntax example: `-pos 34.122222:118.4111111`      |
| `-sf, --scale-factor`         | Scaling factor for position updates (default:`1.0`). Syntax example: `-sf 3.0`.                                                                      |

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

| Option                                 | Description                                                                                                                                                                                                                                                                                                       |
|:---------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-r, --rehanshake-on-update`           | Force rehandshake on registration update. Syntax example:`r`.                                                                                                                                                                                                                                                     |
| `-f, --force-full-handshake`           | By default client will try to resume DTLS session by using abbreviated Handshake. This option force to always do a full handshake. Syntax example:`f`.                                                                                                                                                            |
| `-cid, --connection-id`                | Enable DTLS connection ID (default: off). Control usage of DTLS connection ID: - 'on' to activate Connection ID support (same as -cid 0); - 'off' to deactivate it; - Positive value define the size in byte of CID generated;  0 value means we accept to use CID but will not generated one for foreign peer."  |
| `-c, --cipher-suites`                  | List of cipher suites to use (comma-separated). Define cipher suites to use. CipherCuite enum value separated by ',' without spaces. E.g: TLS_PSK_WITH_AES_128_CCM_8,TLS_PSK_WITH_AES_128_CCM.                                                                                                                    |
| `-oc, --support-deprecated-ciphers`    | Enable support for deprecated cipher suites. Syntax example:`-oc`.                                                                                                                                                                                                                                                |

### Example Commands

#### Cipher suites to use

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coap://demo.thingsboard.io -n MyClientNoSec -c TLS_PSK_WITH_AES_128_CCM_8,TLS_PSK_WITH_AES_128_CCM
```

#### DTLS Connection ID (CID) support

* - 'on' to activate Connection ID support (same as `-cid 0`). `0` value means we accept to use CID but will not generated one for foreign peer.

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coaps://demo.thingsboard.io -n MyClientPsk --psk-identity myIdentity --psk-key mySecret -cid 0
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
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coaps://demo.thingsboard.io -n MyClientPsk --psk-identity myIdentity --psk-key mySecret -cid 4
```

#### Register with the ThingsBoard server (mode NoSec):

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coap://demo.thingsboard.io -n MyClientNoSec
```
or

```sh
docker run --rm -it thingsboard/tb-lwm2m-demo-client:latest -u coap://demo.thingsboard.io -n MyClientNoSec
```

#### Register with the ThingsBoard server (mode with DTLS):

| Option                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                          |
|:----------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-i, --psk-identity`              | Set the LWM2M or Bootstrap server PSK identity in ascii.                                                                                                                                                                                                                                                                                                                                                                             |
| `-p, --psk-key`                   | Set the LWM2M or Bootstrap server Pre-Shared-Key in hexa.                                                                                                                                                                                                                                                                                                                                                                            |
| `-cprik, --client-private-key`    | The path to your client private key file, The private key should be in PKCS#8 format (DER encoding).                                                                                                                                                                                                                                                                                                                                 |
| `-cpubk, --client-public-key`     | The path to your client public key file. The public Key should be in SubjectPublicKeyInfo format (DER encoding).                                                                                                                                                                                                                                                                                                                     |
| `-spubk, --server-public-key`     | "The path to your server public key file. The public Key should be in SubjectPublicKeyInfo format (DER encoding).                                                                                                                                                                                                                                                                                                                    |
| `-ccert, --client-certificate`    | The path to your client certificate file.", The certificate Common Name (CN) should generaly be equal to the client endpoint name (see -n option).", The certificate should be in X509v3 format (DER encoding).                                                                                                                                                                                                                      |
| `-scert, --server-certificate`    | The path to your server certificate file (see -certificate-usage option). The certificate should be in X509v3 format (DER encoding).                                                                                                                                                                                                                                                                                                 |
| `-cu, --certificate-usage`        | Certificate Usage (as integer) defining how to use server certificate",  - 0 : CA constraint";  - 1 : service certificate constraint; - 2 : trust anchor assertion"; - 3 : domain issued certificate (Default value)[Usage are described at](https://tools.ietf.org/html/rfc6698#section-2.1.1)                                                                                                                                      |
| `-ts, --truststore`               | The path to  : ", - a root certificate file to trust, - OR a folder containing trusted certificates, - OR trust store URI; Certificates must be in in X509v3 format (DER encoding); URI format:  file://## Where :  - path-to-store is path to pkcs12 trust store file, - alias-pattern can be used to filter trusted certificates and can also be empty to get all,  Default: empty store.                                          |

##### Use DTLS with PSK authentication:

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coaps://demo.thingsboard.io -n MyClientPsk --psk-identity myIdentity --psk-key mySecret
```

##### Use DTLS with RPK authentication:

Use CoAP over DTLS with Raw Public Key, -cpubk -cprik -spubk options should be used together. [RPK](https://github.com/eclipse/leshan/wiki/Credential-files-format)

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coaps://demo.thingsboard.io -n MyClientRpk -cpubk ./clietPubK.der -cprik ./clientKey.der -spubk ./serverPubK.der
```

##### Use DTLS with X509 authentication:

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coaps://demo.thingsboard.io -n MyClientX509 -ccert ./clientX509v3.der -scert ./serverX509v3.der (optional)-cu 2
```

## Notes

- Ensure the server URL includes the correct scheme (`coap://` or `coaps://`).
- When using DTLS, specify authentication credentials (PSK, RPK, or X.509).
- Custom LwM2M object models should be in OMA DDF format and placed in the specified directory.

For further details, refer to the [ThingsBoard Documentation](https://thingsboard.io/docs/).

## How to activate more log?

This program uses [SLF4J](https://en.wikipedia.org/wiki/SLF4J) as a logging facade, meaning that you can use any compatible backend for your application.

Our demos use the [logback](https://logback.qos.ch/) backend. A verbosity option (`-v, -vv`, ...) allows changing the log level.

To activate more logs for these demos, see [More logs on ThingsBoard LwM2M Demo Client](#more-logs-on-thingsboard-lwm2m-demo-client) or read the [logback documentation](https://logback.qos.ch/manual/configuration.html).

### More logs on ThingsBoard LwM2M Demo Client

#### After start with options to increase verbosity. For example, `-v` or `-vv` or `-vvv`, see [How to activate more log?](#how-to-activate-more-log)

```sh
java -jar thingsboard-lwm2m-demo-client-{version}.jar -u coap://demo.thingsboard.io -n MyClientNoSec -v
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
java -Dlogback.configurationFile="logback-config.xml" -jar thingsboard-lwm2m-demo-client-{version}.jar
```

And in your logback config:

```xml
<configuration>
        <appender name="CLI" class="org/thingsboard/lwm2m/demo/client/logging/TBConsoleAppenderCLI">
                <encoder>
                        <pattern>%d %p %C{0} - %m%n</pattern>
                </encoder>
        </appender>

    <root level="INFO">
        <appender-ref ref="CLI"/>
    </root>
</configuration>

```

### Enables you to execute some dynamic commands from the Interactive Console.

- The dynamic commands from the Interactive Console mode only works when the **-cli**, **--command-line-interactive** option is _enabled_. 
- The CLI log timeout can be customized using **-tcli**, **--time-out-cli**. Init in second. Default value is 5 sec.
  
**Note:**: Specifies how often buffered logs are flushed to the screen when no commands are entered in interactive command-line mode or when the time allocated for typing a command expires before the next log output.

```shell
 docker run --rm -it thingsboard/tb-lwm2m-demo-client:latest -u coap://demo.thingsboard.io -n MyClientNoSec -cli
```

```shell
 docker run --rm -it thingsboard/tb-lwm2m-demo-client:latest -u coap://demo.thingsboard.io -n MyClientNoSec -cli -tcli 10
```

| Commands:                                          | Description                                                                                                                                                                                                         |
|:---------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `help`                                             | Display help information about the specified command.                                                                                                                                                               |
| `list`                                             | List available Objects, Instances and Resources. For example:`list` or `list 5` for ObjectId.                                                                                                                       |
| `create`                                           | Enable a new Object. Format:`create ${ObjectId}`. For example: `create 5`, the Object with ID = `5` is created with the latest available version, `1.2`. or `create 5 1.1` created ObjectId = `5` with ver = `1.1`. |
| `delete`                                           | Desable a Object. . Format:`delete ${ObjectId}`. For example: `delete 5`, the Object with ID = `5` is desabled.                                                                                                     |
| `send current-value`  <br/> `send collected-value` | Send data to server. Usage:  send (current-value or collected-value)  [-c=<contentFormat>]                                                                                                                          |                                                                                                                                                       |
| `collect`                                          | Collect data to send it later with 'send' command                                                                                                                                                                   |
| `move`                                             | Simulate client mouvement.                                                                                                                                                                                          |
| `updateRegistration`                               | Trigger a registration update.                                                                                                                                                                                      |
| `reboot`                                           | Restart client without update object.                                                                                                                                                                               |
| `stop`                                             | Stop the CLI.                                                                                                                                                                                                       |

**Note:** 
- Press **'Ctrl-V'** once to paste text.
- Press **'Ctrl-C'** once to copy text.
- Press **'Ctrl-C'** **'Ctrl-C'** quickly to exit the demo client.
- Use the CLI command **'stop'** to pause reading input from the console without exiting the application.


#### move

* `-d` or `north` - Move to the North, For example: `move -d`, result in objectId = 6 (MyLocation): _latitude_ = **latitude** + `1.0f` * **scaleFactor**;
* `-e` or `east` - Move to the East, For example: `move -e`, result in objectId = 6 (MyLocation): _longitude_ = **longitude** + `-1.0f` * **scaleFactor**;
* `-s` or `south` Move to the South, For example: `move -s`, result in objectId = 6 (MyLocation): _latitude_ = **latitude** + `-1.0f` * **scaleFactor**;
* `-w` or `west` Move to the West, For example: `move -w`, result in objectId = 6 (MyLocation): _longitude_ = **longitude** + `1.0f` * **scaleFactor**;

#### send

Explanation:

**send** → Sends data to the server.
*/3303/0/5700*=`13.6` → Specifies the LwM2M resource to update:

| Params   | Description                     |
|:---------|:--------------------------------|
| `3303`   | Object ID (Temperature Sensor). |
| `0`      | Instance ID.                    |
| `5700`   | Resource ID (Sensor Value).     |
| `13.6`   | Current value for the resource. |

```sh
send current-value /3303/0/5700
```

```sh
collect /3303/0/5700 /3303/0/5701
send collected-value /3303/0/5700 /3303/0/5701
```

#### collect

After collecting the data, use the send command to transmit all stored values.

Explanation:

**collect** → Stores the specified value without immediately sending it to the server.

*/3303/0/5700*=`22.5` → Specifies the LwM2M resource to collect:

| Params   | Description                              |
|:---------|:-----------------------------------------|
| `3303`   | Object ID (Temperature Sensor).          |
| `0`      | Instance ID.                             |
| `5700`   | Resource ID (Sensor Value).              |
| `22.5`   | Current value for the resource with Time |

```sh
collect /3303/0/5700
collect /3323/1/5601 /3323/1/5602
```

**This sequence:**

* _Collect_ `15.7` as the Min Value for Power Measurement.
* _Collect_ `48.2` as the Max Value for Power Measurement, and other...
* _Sends_ `all` collected `all values are sent in one request at once, each value will have its own time`.

```sh
send collected-value /3303/0/5700 /3323/1/5601 /3323/1/5602
````

## 🚀 Building from Sources

### 🔧 Clone the repository

```bash
git clone https://github.com/thingsboard/thingsboard.lwm2m.demo.client.git
cd thingsboard.lwm2m.demo.client
```

### 🛠 Build the project

```bash
mvn clean install
```

or, to skip tests:

```bash
mvn clean package -DskipTests
```

---

## ▶️ Running as a Plain Java Application

### 📄 Using the JAR file

The built JAR is located at:

```
target/thingsboard-lwm2m-demo-client-4.1.2.jar
```

or 

```
target/thingsboard-lwm2m-demo-client-{version}.jar
```

#### 🔹 Default run

```bash
java -jar thingsboard-lwm2m-demo-client-4.1.2.jar
```

or

```bash
java -jar thingsboard-lwm2m-demo-client-{version}.jar
```

#### 🔹 Run in NoSec mode (URL = local server: localhost; port = 5685)

```bash
java -jar thingsboard-lwm2m-demo-client-4.1.2.jar -u coap://localhost:5685 -n MyClientNoSec
```

#### 🔹 Run in DTLS (PSK) mode (URL = local server: localhost; port = 5686)

```bash
java -jar thingsboard-lwm2m-demo-client-4.1.2.jar -u coaps://localhost:5686 -n MyClientPsk -i myIdentity -p 01020304050607080A0B0C0D0F010203
```

#### 🔹 Run in NoSec mode (URL = demo.thingsboard.io)

```bash
java -jar thingsboard-lwm2m-demo-client-4.1.2.jar -u coap://demo.thingsboard.io -n MyClientNoSec
```

#### 🔹 Run in DTLS (PSK) mode (URL = demo.thingsboard.io)

```bash
java -jar thingsboard-lwm2m-demo-client-4.1.2.jar -u coaps://demo.thingsboard.io -n MyClientPsk --psk-identity myIdentity --psk-key 01020304050607080A0B0C0D0F010203
```

---

## 🐳 Running with Docker

**Important:** When running the tb-lw-demo-client in a Docker container, do not use **localhost**, 127.0.0.1, or your host's 192.168.x.x IP for the LwM2M Server address.
These will not resolve correctly from within the container.

Correct way (on Linux):

Use the Docker bridge gateway IP — usually 172.17.0.1 (the **docker0** interface):

This address represents the host machine as seen from inside Docker.

```shell
#Command:
ip a | grep docker0

# Requst
13: docker0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default 
    link/ether ...
    inet 172.17.0.1/16 brd 172.17.255.255 scope global docker0
    ...
```

### 🔹 NoSec mode (Docker + host IP):

- localhost -> Address = 172.17.0.1; Port = 5685.
```bash
docker run --rm -it thingsboard/tb-lwm2m-demo-client:latest -u coap://172.17.0.1:5685 -n MyClientNoSec
```

- URL = demo.thingsboard.io
```bash
docker run --rm -it thingsboard/tb-lwm2m-demo-client:latest -u coap://demo.thingsboard.io -n MyClientNoSec
```

### 🔹 DTLS (PSK) mode:

- localhost -> Address = 172.17.0.1; Port = 5686.
```bash
docker run --rm -it thingsboard/tb-lwm2m-demo-client:latest -u coaps://172.17.0.1:5686 -n 	MyClientPsk -i myIdentity -p 01020304050607080A0B0C0D0F010203
```

- demo.thingsboard.io

```bash
docker run --rm -it thingsboard/tb-lwm2m-demo-client:latest -u coaps://demo.thingsboard.io -n MyClientPsk -i myIdentity -p 01020304050607080A0B0C0D0F010203
```

## 📁 Project Structure

```
Thingsboard Lwm2m Demo Client
├── ThingsboardLwDemoCient.java
├── cli/
│   └── CommandLineRunnerImpl.java
├── core/
│   └── ClientFactory.java
├── service/
│   ├── LwM2mClientService.java
│   └── ShutdownHandler.java
```
