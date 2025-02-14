# Open Data Mesh Command Line

<!-- TOC -->

* [Open Data Mesh Command Line](#open-data-mesh-command-line)
    * [About](#about)
    * [Installation](#installation)
    * [Configuration](#configuration)
    * [Usage](#usage)
    * [Commands](#commands)
    * [Options](#options)
        * [Common options](#common-options)

<!-- TOC -->

## About

The Open Data Mesh Command Line tool provides a range of functionalities to support users within the Open Data Mesh
ecosystem. Key features include an efficient way to create, validate, and manage data product descriptors, ensuring
compliance with Open Data Mesh standards.

## Setup

---
To run the application properly, you must have a Java JDK installed.

- **Linux**
  ```sh
  sudo apt update && sudo apt install -y openjdk-17-jdk
  ```
- **Windows**
  ```powershell
  winget install --id Oracle.JDK.17 -e
  ```

---

Download the cli.

```bash
wget -qO odm-cli $(wget -qO- https://api.github.com/repos/opendatamesh-initiative/odm-cli/releases/latest | grep "browser_download_url.odm-cli" | cut -d '"' -f 4)
```

---
Test the cli.

```bash
./odmcli --version
```

## Configuration

By default, the `odmcli` stores its configuration file in a directory called `.odmcli` within your `$HOME` directory.
The

## Usage

`odmcli [COMMAND] [OPTIONS] [ARGS]`

Manage local env and all interactions with the remote ODM Platform's services

## Commands

| Command                             | Description      |
|-------------------------------------|------------------|
| [`odmcli local`](docs/cmd-local.md) | Manage local env |

## Options

| Command         | Default | Description           |
|-----------------|---------|-----------------------|
| `--version, -v` |         | Version o the command |