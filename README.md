# Open Data Mesh Command Line

<!-- TOC -->

* [Open Data Mesh Command Line](#open-data-mesh-command-line)
    * [About](#about)
    * [Setup](#setup)
    * [Updates](#updates)
    * [Configuration](#configuration)
    * [Usage](#usage)
        * [Commands](#commands)
        * [Options](#options)
        * [Exit codes](#exit-codes)

<!-- TOC -->

## About

The Open Data Mesh Command Line tool provides a range of functionalities to support users within the Open Data Mesh
ecosystem. Key features include an efficient way to create, validate, and manage data product descriptors, ensuring
compliance with Open Data Mesh standards.

## Setup

---
To run the application properly, you must have **Java 21 or newer** installed.

  ```sh
  sudo apt update && sudo apt install -y openjdk-21-jdk
  ```

If your distribution does not provide an `openjdk-21-jdk` package (e.g. Debian 12), install a Java 21 distribution
such as [Eclipse Temurin](https://adoptium.net/installation/linux/) or use [SDKMAN!](https://sdkman.io/).
You can check the installed version with `java -version`.

---

Download the cli.

```bash
wget -qO odm-cli $(wget -qO- https://api.github.com/repos/opendatamesh-initiative/odm-cli/releases/latest | grep -Eo '"browser_download_url": *"[^"]+/odm-cli"' | sed -E 's/.*"([^"]+)".*/\1/' | head -n1) && chmod +x odm-cli
```

---
Test the cli.

```bash
./odm-cli --version
```

### Run with Docker

The CLI is also available as a Docker image, with Java included:

```bash
docker run --rm -v "$PWD":/workspace -w /workspace opendatamesh/odm-cli:X.Y.Z --version
```

In a CI job, use it as the job image and call `odm-cli` in the script. Configuration, extensions and certificates are in
[Run with Docker](docs/run-with-docker.md); pipeline examples for the DevOps commands are in
[Run DevOps tasks from a pipeline](docs/devops-pipelines.md).

## Updates

Each released `odm-cli` script is bound to the version it was released with:

- **Pinned Version**: During the release, the version is injected into the `VERSION` variable of the `odm-cli` script.
  When you run the CLI, the script downloads the corresponding JAR file (`odm-cli-{version}.jar`) to `~/.odmcli/` and
  runs it. The script does not switch to a newer version on its own.
- **Version Caching**: The CLI checks if the pinned version is already installed by comparing the version stored in
  `~/.odmcli/version.txt` with the script's version, and only downloads the JAR if it's not already present.
- **Update Notifications**: The CLI fetches the latest release version from the GitHub API. If a newer version is
  available than the one you're running, it displays a notification:
  "A newer version ({version}) is available. Consider upgrading!"
- **Upgrading**: To upgrade, download the latest `odm-cli` script again using the command in the [Setup](#setup)
  section. The new script will download the new JAR on its first run.
- **Rollback Support**: To roll back to a previous version, download the `odm-cli` script of that release, or modify
  the `VERSION` variable in the `odm-cli` executable script.

The `odm-cli` script in the repository (not attached to a release) uses `VERSION="latest"` and always downloads the
latest released JAR.

> **Java requirement**: Upgrading does **not** install or update Java. Since version 2.0.0, the CLI requires Java 21
> or newer: before upgrading from an older version, make sure Java 21 is installed (see [Setup](#setup)). If an
> incompatible Java version is found, the `odm-cli` script stops with an error message. Previous releases also run on
> Java 21.

## Configuration

By default, `odmcli` stores its configuration file in a directory named `.odmcli` within your `$HOME` directory.  
To override the default configuration, add or modify the `application.yml` file in this directory.  
An example configuration file is shown below:

```yaml
cli:
  cliConfiguration:
    templatesFolder: '/path/to/templates'
    saveFormat: NORMALIZED
    interactive: true
  systems:
    - name: testDb
      endpoint: 'jdbc:mysql://localhost:3306/'
      user: 'root'
      password: 'root'
      driver: JDBC
    - name: anotherDb
      endpoint: 'jdbc:mysql://localhost:3306/'
      user: 'root'
      password: 'root'
      driver: JDBC
  template:
    owner:
      id: "ownerId"
      name: "ownerName"
  <customEnv>:
    <customEnvField>: <customEnvValue>
extensions:
  - name: odm-cli-extensions-starter-1.1.0
    url: https://github.com/opendatamesh-initiative//odm-cli-extensions-starter/releases/download/v1.1.0/odm-cli-extensions-starter-1.1.0.jar
```

Where:

- **cli.configuration**: Internal CLI settings that define default parameter values.
    - **templatesFolder**: the (default) directory where templates for Data Product Descriptor elements are stored.
    - **saveFormat**: the (default) format in which the data product descriptor is saved
    - **interactive**:  the (default) behavior of the CLI. When set to `false`, the CLI will not prompt the user for
      required options and will fail if they are not provided.
- **cli.systems**:
    - **name**: the configuration name, used for retrieval.
    - **endpoint**: the connection url of the remote system.
    - **user** & **password** : credentials needed for authentication.
    - **driver**: the driver name (if required for the connection).
- **cli.template**:
  - **owner**: the default owner for data products.
- **\<customEnv>**: section for additional default values used by CLI extensions.
- **extensions**: configuration for automatically downloading CLI extensions jar files.
    - **name**: the name of the extension (should be unique).
    - **url**: the url for downloading the .jar file of the extension.

The ODM Platform service URLs and credentials (`cli.platform`) are described in [odmcli platform](docs/cmd-platform.md).
They are not packaged with the CLI: set them in this file or with environment variables.

### Environment variables

Every configuration key can also be set with an environment variable, with the executable and with the Docker image.
**An environment variable wins over the configuration file**, and the configuration file is used for the keys that no
variable sets. Command options, where a command has one (such as `--poll-interval`), win over both.

This is the usual way to configure the CLI in a pipeline: set the variables once, as CI variables shared by all
pipelines, and store credentials as masked variables or secrets.

The variable name is the key in upper case, with `.` replaced by `_`, dashes removed, and camelCase words merged
(`pollInterval` → `POLLINTERVAL`). A list element is written with its index (`HEADERS_0_NAME`), and a list set this way
replaces the whole list of the file.

| Variable | Key | Purpose |
| --- | --- | --- |
| `CLI_PLATFORM_SERVICES_REGISTRY_URL` | `cli.platform.services.registry.url` | Registry URL. |
| `CLI_PLATFORM_SERVICES_DEVOPS_URL` | `cli.platform.services.devops.url` | DevOps URL. |
| `CLI_PLATFORM_SERVICES_<REGISTRY\|DEVOPS>_OAUTH2_URL`, `…_GRANTTYPE`, `…_SCOPE`, `…_CLIENTID`, `…_CLIENTSECRET`, `…_CLIENTCERTIFICATE`, `…_CLIENTCERTIFICATEPRIVATEKEY` | `cli.platform.services.<service>.oauth2.*` | OAuth2 authentication, when the platform requires it. |
| `CLI_PLATFORM_SERVICES_<REGISTRY\|DEVOPS>_HEADERS_<n>_NAME`, `…_HEADERS_<n>_VALUE` | `cli.platform.services.<service>.headers[n]` | Extra request headers, for example an API key. |
| `CLI_PLATFORM_DEVOPS_POLLINTERVAL`, `…_ACTIVITYAPPROVALTIMEOUT`, `…_TASKAPPROVALTIMEOUT`, `…_RUNIDLOOKUPATTEMPTS`, `…_RUNIDLOOKUPINTERVAL`, `…_CONFLICTRETRIES`, `…_CONFLICTRETRYINTERVAL` | `cli.platform.devops.*` | Timings of the devops commands. |
| `CLI_CLICONFIGURATION_INTERACTIVE` | `cli.cliConfiguration.interactive` | Set it to `false` in CI, so the CLI never waits for input. |

## Usage

`odmcli [COMMAND] [OPTIONS]`

### Commands

| Command                                   | Description                                                      |
|-------------------------------------------|------------------------------------------------------------------|
| [`odmcli config`](docs/cmd-config.md)     | Manages the configuration file.                                  |
| [`odmcli local`](docs/cmd-local.md)       | Manages features that are executed within the local environment. |
| [`odmcli platform`](docs/cmd-platform.md) | Command to manage ODM Platform functionalities.                  |

### Options

| Command             | Default                                               | Description                                                                                                        |
|---------------------|-------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `--version, -V`     |                                                       | Version of the command                                                                                             |
| `--interactive, -i` | true (can be overwritten in the application.yml file) | When set to `false`, the CLI will not prompt the user for required options and will fail if they are not provided. |
### Exit codes

| Code | Meaning                                                                                                     |
|------|-------------------------------------------------------------------------------------------------------------|
| `0`  | The command completed successfully.                                                                         |
| `1`  | The command failed (e.g. invalid or missing descriptor file, ODM Platform service error, a required value not provided with `--interactive=false`). |
| `2`  | Invalid usage detected while parsing the command line: unknown option or missing required option.          |

Before version 2.0.0 the CLI always exited with `0`, even when a command failed.
