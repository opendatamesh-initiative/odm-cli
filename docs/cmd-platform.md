# odmcli platform

## Usage

`odm-cli platform `

## Description

Command to manage ODM Platform functionalities.

**Note:** To function correctly, the CLI must be properly configured and have access to the ODM Platform REST API
endpoints. Those can be configured on the cli configuration file.

```yaml
cli:
  #[ ... ]
  odmPlatform:
    registryService:
      endpoint: http://localhost:8001
    #[ ... ]
```

## Commands

| Command                                                | Description                                                                         |
|--------------------------------------------------------|-------------------------------------------------------------------------------------|
| [`odmcli platform registry`](cmd-platform-registry.md) | Command to manage the ODM Platform functionalities exposed by the Registry Service. | |

## Options

| Command         | Description                                |
|-----------------|--------------------------------------------|
| `--version, -v` | Prints the current version of the command. |



