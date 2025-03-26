# odmcli local

## Usage

`odm-cli local`

## Description

Manage local env

## Options

| Command             | Default    | Description                                                                                                                                                                                                                                                                    |
|---------------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `--save-format, -s` | NORMALIZED | Specifies the format in which the descriptor could be saved. If set to `normalized`, each descriptor component is stored as a separate file, and the `ref` field is populated. If set to `canonical`, all the `ref` are loaded and the descriptor is saved as one single file. |
| `--version, -v`     |            | Version o the command                                                                                                                                                                                                                                                          |

## Subcommands

| Command                                      | Description                                          |
|----------------------------------------------|------------------------------------------------------|
| [`odmcli local import`](cmd-local-import.md) | Imports a descriptor element from an external source |
| [`odmcli local init`](cmd-local-init.md)     | Initializes a new data product descriptor template with the given domain and name |
