# Open Data Mesh Command Line

<!-- TOC -->

* [Open Data Mesh Command Line](#open-data-mesh-command-line)
    * [About](#about)
    * [Setup](#setup)
    * [Configuration](#configuration)
    * [Usage](#usage)
        * [Commands](#commands)
        * [Options](#options)

<!-- TOC -->

## About

The Open Data Mesh Command Line tool provides a range of functionalities to support users within the Open Data Mesh
ecosystem. Key features include an efficient way to create, validate, and manage data product descriptors, ensuring
compliance with Open Data Mesh standards.

## Setup

---
To run the application properly, you must have a Java JDK installed.

  ```sh
  sudo apt update && sudo apt install -y openjdk-17-jdk
  ```

---

Download the cli.

```bash
wget -qO odm-cli $(wget -qO- https://api.github.com/repos/opendatamesh-initiative/odm-cli/releases/latest | grep -Eo '"browser_download_url": *"[^"]+"' | grep odm-cli | sed -E 's/.*"([^"]+)".*/\1/' | head -n1) && chmod +x odm-cli
```

---
Test the cli.

```bash
./odmcli --version
```

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
| `--version, -v`     |                                                       | Version o the command                                                                                              |
| `--interactive, -i` | true (can be overwritten in the application.yml file) | When set to `false`, the CLI will not prompt the user for required options and will fail if they are not provided. |