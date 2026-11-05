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
        url: https://odm.example.com/registry #(mandatory for the registry commands)
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
      devops: #(mandatory for the devops commands) Same shape as registry
        url: https://odm.example.com/devops
        headers: []
        oauth2:
    devops: #(optional) Timings of the devops commands, shown with their defaults
      pollInterval: 5s
      activityApprovalTimeout: 30m
      taskApprovalTimeout: 10m
      runIdLookupAttempts: 5
      runIdLookupInterval: 2s
      conflictRetries: 3
      conflictRetryInterval: 1s
    #[ ... ]
```

**The `url` is the base address only.** It is the host of the service, or the prefix of the Agent proxy that forwards to it,
without any `/api/...` path. This holds for `registry` (the V1 validate call and the V2 lookups) and for `devops`. The
commands add the API paths themselves.

The service URLs are environment specific, so they are not packaged with the CLI: set them in the configuration file or
with the environment variables `CLI_PLATFORM_SERVICES_REGISTRY_URL` and `CLI_PLATFORM_SERVICES_DEVOPS_URL`. A command that
runs without the URL it needs fails and names the variable and the key. An environment variable wins over the file (see
[Environment variables](../README.md#environment-variables)), so a pipeline can set the URLs once, as CI variables.

The `cli.platform.devops` timings are safe everywhere and have the defaults above. Durations are written as `30s`, `10m`
or `PT10M`.

## Commands

| Command                                                | Description                                                                         |
|--------------------------------------------------------|-------------------------------------------------------------------------------------|
| [`odmcli platform registry`](cmd-platform-registry.md) | Command to manage the ODM Platform functionalities exposed by the Registry Service. | |
| [`odmcli platform devops`](cmd-platform-devops.md)     | Command to run tasks of the ODM Platform DevOps Service from a pipeline.             | |

## Options

| Command         | Description                                |
|-----------------|--------------------------------------------|
| `--version, -v` | Prints the current version of the command. |



