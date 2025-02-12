# odmcli local

## Usage

`odmcli local`

## Description

Manage local env

## Options

| Command             | Default   | Description                                                                                                                                                                                                                                                                    |
|---------------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `--save-format, -s` | canonical | Specifies the format in which the descriptor could be saved. If set to `normalized`, each descriptor component is stored as a separate file, and the `ref` field is populated. If set to `canonical`, all the `ref` are loaded and the descriptor is saved as one single file. |
| `--version, -v`     |           | Version o the command                                                                                                                                                                                                                                                          |

## Subcommands

| Command                                          | Description                                          |
|--------------------------------------------------|------------------------------------------------------|
| [`odmcli local init`](cmd-local-init.md)         | Initializes a descriptor                             |
| [`odmcli local list`](cmd-local-list.md)         | Lists elements of a descriptor                       |
| [`odmcli local get`](cmd-local-get.md)           | Gets an element from descriptor                      |
| [`odmcli local add`](cmd-local-add.md)           | Adds an element to descriptor                        |
| [`odmcli local rm`](cmd-local-rm.md)             | Removes an element from descriptor                   |
| [`odmcli local set`](cmd-local-set.md)           | Sets a field value of a descriptor                   |
| [`odmcli local lint`](cmd-local-lint.md)         | Lints a descriptor                                   |
| [`odmcli local validate`](cmd-local-validate.md) | Validates a descriptor                               |
| [`odmcli local import`](cmd-local-import.md)     | Imports a descriptor element from an external source |
| [`odmcli local export`](cmd-local-export.md)     | Exports a descriptor element to an external target   |

