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
  platform:
    services:
      registry:
        url: http://localhost:8001 #(mandatory)
        headers: #(optional) Can be used to authenticate via ApiKey
          - name: 
            value: 
        oauth2: #(optional) Can be used to authenticate via Oauth2
          url: 
          grantType: 
          scope: 
          clientId: 
          clientSecret: 
          clientCertificate: 
          clientCertificatePrivateKey:         
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



