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

This repository is the home of the Open Data Mesh CLI.

## Installation

Download the cli

```bash
echo TODO
```

or compile it from the code

```bash
git clone git@github.com:opendatamesh-initiative/odm-platform.git
cd odm-cli
mvn clean package spring-boot:repackage
cd odm-cli
```

test the cli

```bash
./odmcli --version
```

## Configuration

By default, the `odmcli` stores its configuration files in a directory called `.odmcli` within your `$HOME` directory.

`odmcli` manages most of the files in the configuration directory and you shouldn't modify them. However, you can modify
the `config.json` file to control certain aspects of how the `odmcli` command behaves.

You can modify the `odmcli` command behavior using environment variables or command-line options. You can also use
options within `config.json` to modify some of the same behavior. If an environment variable and the `--config` flag are
set, the flag precedes the environment variable. Command line options override environment variables and environment
variables override properties you specify in a `config.json` file.

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