# Run with Docker

The CLI is also published as a Docker image, so it runs without installing Java or the `odm-cli` script. The image
contains the released jar and a Java 21 runtime (Amazon Corretto). It runs exactly the same commands, with the same
options and exit codes.

```
opendatamesh/odm-cli:X.Y.Z
```

Each release is published once, as `X.Y.Z`, for `linux/amd64`. The tag never changes, so pin it in pipelines.

## Using the image

`docker run <image> <arguments>` is the same as `odm-cli <arguments>`. The CLI reads files relative to its working
directory, so mount the directory with your files and make it the working directory:

```bash
docker run --rm -v "$PWD":/workspace -w /workspace \
  -e CLI_PLATFORM_SERVICES_REGISTRY_URL=https://odm.example.com/registry \
  opendatamesh/odm-cli:X.Y.Z platform registry validate -f data-product-descriptor.json
```

- **Paths** inside the container are not host paths: `~` is the container user's home, and an absolute host path does not
  exist unless it is mounted.
- **User**: the image runs as a non-root user (UID 10001). If the CLI must write into the mounted directory (`local init`,
  `local import`), run it with your own user: `docker run --user "$(id -u):$(id -g)" …`.
- **No prompts**: the image sets `CLI_CLICONFIGURATION_INTERACTIVE=false`, so a missing value fails instead of waiting
  for input. Set it to `true` only with `docker run -it`.
- **CI jobs**: when a CI job uses the image as its job image, the job script calls `odm-cli` exactly like the executable.
  No `entrypoint` override is needed, in GitLab or in GitHub Actions.

## Configuration

Every configuration key can be set with an environment variable or in the configuration file, and **the environment
variable wins**. In Docker, use environment variables: they need no mount, and CI tools store them as secrets.

```bash
docker run --rm -v "$PWD":/workspace -w /workspace \
  -e CLI_PLATFORM_SERVICES_DEVOPS_URL=https://odm.example.com/devops \
  -e CLI_PLATFORM_SERVICES_REGISTRY_URL=https://odm.example.com/registry \
  opendatamesh/odm-cli:X.Y.Z platform devops complete-task --provider-run-id 1234 --status SUCCEEDED
```

The variable names and the naming rule are in the README, under
[Environment variables](../README.md#environment-variables). The service URLs are not packaged: a command that needs a
service whose URL is not set fails and names the variable to set.

Variables that exist only in the image:

| Variable | Purpose |
| --- | --- |
| `ODM_CLI_CONFIG_FILE` | Configuration file, default `$HOME/.odmcli/application.yml` (`/home/odmcli/.odmcli/application.yml`). |
| `ODM_CLI_EXTENSIONS_DIR` | Extensions directory, default `/opt/odm-cli/extensions`. |
| `JAVA_TOOL_OPTIONS` | JVM options, for example a proxy: `-Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=3128`. |

**Configuration file**: as the fallback, or for list values (`cli.systems`, headers), which are easier to write in a file.
Mount it at the default location, or point `ODM_CLI_CONFIG_FILE` to it:

```bash
docker run --rm -v "$PWD":/workspace -w /workspace \
  -v "$HOME/.odmcli/application.yml":/home/odmcli/.odmcli/application.yml:ro \
  opendatamesh/odm-cli:X.Y.Z platform registry validate
```

`config --set` and `config --unset` edit that file. Without a mount, the edits are lost when the container exits.

## Extensions and certificates

No extensions are bundled and none are downloaded. To use one, build your own image on top of the CLI image:

```dockerfile
FROM opendatamesh/odm-cli:X.Y.Z
COPY my-extension.jar /opt/odm-cli/extensions/
```

or mount a directory of extension jars at `/opt/odm-cli/extensions`.

The Java runtime uses the system trust store of the image. To trust a private CA, add it in your own image:

```dockerfile
FROM opendatamesh/odm-cli:X.Y.Z
USER root
COPY my-ca.crt /etc/pki/ca-trust/source/anchors/my-ca.crt
RUN update-ca-trust
USER 10001:10001
```

or mount a trust store and set `JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStore=/path/to/truststore`.

## Exit codes

The container exits with the CLI's exit code: `0` success, `1` command failure, `2` invalid usage. A pipeline step fails
whenever the code is not `0`.

## DevOps pipelines

Use the image as the job image of every job that calls the DevOps commands, for both instrumented and full-control tasks.
`start-task` and `complete-task` run in their own jobs, so the CLI never has to be in your build image, and your build job
keeps its own image.

Examples for GitLab CI and GitHub Actions, the value to pass on each provider, and how to handle a cancelled run are in
[Run DevOps tasks from a pipeline](devops-pipelines.md).
