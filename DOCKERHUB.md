# ODM CLI

The ODM CLI is the command line of [Open Data Mesh Platform](https://github.com/opendatamesh-initiative). It validates data product descriptors against the policies of the platform, creates and imports descriptors locally, and runs DevOps tasks from CI pipelines: `start-task` and `complete-task` keep the ODM [DevOps server](https://hub.docker.com/r/opendatamesh/odm-platform-devops) in sync with a GitLab CI or GitHub Actions run. This image contains the released CLI and a Java 21 runtime (Amazon Corretto), so it runs without installing Java.

- **Source code:** [opendatamesh-initiative/odm-cli](https://github.com/opendatamesh-initiative/odm-cli)
- **Documentation:** [Run with Docker](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/run-with-docker.md) and [README](https://github.com/opendatamesh-initiative/odm-cli/blob/main/README.md)
- **Issues:** [GitHub issues](https://github.com/opendatamesh-initiative/odm-cli/issues)

## Tags

Each [GitHub release](https://github.com/opendatamesh-initiative/odm-cli/releases) publishes one image, for `linux/amd64`, tagged with its version without the `v` prefix:

```bash
docker pull opendatamesh/odm-cli:<version>
```

There is no `latest` tag and a tag never changes: pin it in pipelines. The first image is `2.0.0`.

## Quick start

The image has no entrypoint: `docker run <image> odm-cli <arguments>` is the same as running `odm-cli <arguments>`, and `docker run <image>` alone prints the help. Mount the directory with your files at `/workspace`, the working directory:

```bash
docker run --rm -v "$PWD":/workspace \
  -e CLI_PLATFORM_SERVICES_REGISTRY_URL=https://odm.example.com/registry \
  opendatamesh/odm-cli:<version> odm-cli platform registry validate -f data-product-descriptor.json
```

Files are read from `/workspace`; paths outside the mounted directory, and `~`, don't exist in the container. The container exits with the CLI's exit code: `0` success, `1` command failure, `2` invalid usage.

## In a CI pipeline

Use the image as the job image of the jobs that call the CLI, and call `odm-cli` in the script. GitLab and GitHub mount the checkout and earlier artifacts in the job, so no mount and no `entrypoint` override are needed. For example, a GitLab job that closes an instrumented DevOps task:

```yaml
odm-complete-succeeded:
  stage: odm-complete
  image: opendatamesh/odm-cli:<version>
  script:
    - odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --status SUCCEEDED
        --logs-file build.log --results-file results.json
```

Set the service URLs once, as CI variables shared by all pipelines, and store credentials as masked variables or secrets. Full GitLab CI and GitHub Actions examples: [Run DevOps tasks from a pipeline](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/devops-pipelines.md).

## Environment variables

Every configuration key can be set with an environment variable or in the configuration file, and the environment variable wins. The variable name is the key in upper case, with `.` replaced by `_`, dashes removed and camelCase words merged (`cli.platform.devops.pollInterval` becomes `CLI_PLATFORM_DEVOPS_POLLINTERVAL`). The service URLs aren't packaged: a command that needs a service whose URL isn't set fails and names the variable to set.

| Variable | Description | Default |
| --- | --- | --- |
| `CLI_PLATFORM_SERVICES_REGISTRY_URL` | Registry URL. | none |
| `CLI_PLATFORM_SERVICES_DEVOPS_URL` | DevOps URL. | none |
| `CLI_PLATFORM_SERVICES_<REGISTRY\|DEVOPS>_OAUTH2_*` | OAuth2 settings (`URL`, `GRANTTYPE`, `SCOPE`, `CLIENTID`, `CLIENTSECRET`, `CLIENTCERTIFICATE`, `CLIENTCERTIFICATEPRIVATEKEY`), when the platform requires them. | none |
| `CLI_PLATFORM_SERVICES_<REGISTRY\|DEVOPS>_HEADERS_<n>_NAME`, `…_VALUE` | Extra request headers, for example the credentials of a Blindata Agent in front of ODM. | none |
| `CLI_PLATFORM_DEVOPS_*` | Timings of the DevOps commands: `POLLINTERVAL`, `ACTIVITYAPPROVALTIMEOUT`, `TASKAPPROVALTIMEOUT`, `RUNIDLOOKUPATTEMPTS`, `RUNIDLOOKUPINTERVAL`, `CONFLICTRETRIES`, `CONFLICTRETRYINTERVAL`. | see the docs |
| `CLI_CLICONFIGURATION_INTERACTIVE` | Prompt for missing values. The image sets it to `false`, so a missing value fails instead of waiting for input; set it to `true` only with `docker run -it`. | `false` |

Variables that exist only in the image:

| Variable | Description | Default |
| --- | --- | --- |
| `ODM_CLI_CONFIG_FILE` | Configuration file. It may be missing. | `/home/odmcli/.odmcli/application.yml` |
| `ODM_CLI_EXTENSIONS_DIR` | Directory of CLI extension jars. | `/opt/odm-cli/extensions` |
| `JAVA_TOOL_OPTIONS` | JVM options, for example a proxy: `-Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=3128`. | empty |

List values, such as `cli.systems`, are easier to set in the configuration file: mount it at the default path or point `ODM_CLI_CONFIG_FILE` to it. The full list of keys is in the [README](https://github.com/opendatamesh-initiative/odm-cli/blob/main/README.md#environment-variables).

## User, volumes and extensions

- **User:** the image runs as a non-root user (UID `10001`). When the CLI must write into the mounted directory (`local init`, `local import`), run it with your own user: `docker run --user "$(id -u):$(id -g)" …`.
- **Volumes:** `/workspace` for your files; optionally the configuration file. Edits made with `config --set` are lost when the container exits unless the file is mounted.
- **Extensions:** none are bundled or downloaded. Mount a directory of extension jars at `/opt/odm-cli/extensions`, or build your own image `FROM opendatamesh/odm-cli:<version>` that copies them there.
- **Private CA:** the Java runtime uses the image's system trust store. Add your CA in your own image (`update-ca-trust` as root), or mount a trust store and set `JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStore=/path/to/truststore`.
- **Ports:** none. The CLI runs a command and exits.

## Learn more

- [Commands](https://github.com/opendatamesh-initiative/odm-cli/blob/main/README.md#commands): [config](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/cmd-config.md), [local](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/cmd-local.md), [platform](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/cmd-platform.md)
- [start-task](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/cmd-platform-devops-start-task.md) and [complete-task](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/cmd-platform-devops-complete-task.md)
- [Registry validate](https://github.com/opendatamesh-initiative/odm-cli/blob/main/docs/cmd-platform-registry-validate.md)

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
