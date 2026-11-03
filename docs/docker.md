# Docker image

The CLI is also published as a Docker image, so pipelines can run it without installing Java or the `odm-cli` script. The
image contains the released jar and a Java 21 runtime (Amazon Corretto), and runs exactly the same commands, with the same
options and exit codes.

```
ghcr.io/opendatamesh-initiative/odm-cli:X.Y.Z
```

## Tags

| Tag | Meaning |
| --- | --- |
| `X.Y.Z` | One release. It never changes: **use it in pipelines.** |
| `X.Y`, `X` | The latest release of that minor or major version. They move with each release. |
| `latest` | The latest release. It moves with each release. |

Prereleases get only their own `X.Y.Z-…` tag. Images are built for `linux/amd64` and `linux/arm64`.

## Running it

`docker run <image> <arguments>` is the same as `odm-cli <arguments>`. The CLI reads files relative to its working
directory, so mount the directory with your files and make it the working directory:

```bash
docker run --rm -v "$PWD":/workspace -w /workspace \
  -e CLI_PLATFORM_SERVICES_REGISTRY_URL=https://odm.example.com/registry \
  ghcr.io/opendatamesh-initiative/odm-cli:X.Y.Z platform registry validate -f data-product-descriptor.json
```

Paths inside the container are not host paths: `~` is the container user's home, and an absolute host path does not exist
unless it is mounted.

The image runs as a non-root user (UID 10001). If the CLI must write into the mounted directory (`local init`,
`local import`), run it with your own user: `docker run --user "$(id -u):$(id -g)" …`.

## GitLab CI

When a job uses the image, its script calls `odm-cli` exactly like the executable. No `entrypoint` override is needed.

**Full-control task**: the job only uploads its results, so it can run in the CLI image.

```yaml
upload-results:
  image: ghcr.io/opendatamesh-initiative/odm-cli:X.Y.Z
  script:
    - ./produce-results.sh > results.json
    - odm-cli platform devops complete-task --provider-run-id "$CI_PIPELINE_ID" --results-only --results-file results.json
```

**Instrumented task**: `start-task`, the build and `complete-task` run in the same job, so the CLI must be in the build
image. Copy it from the CLI image: everything it needs, Java included, is in `/opt/odm-cli`.

```dockerfile
FROM maven:3-eclipse-temurin-21
COPY --from=ghcr.io/opendatamesh-initiative/odm-cli:X.Y.Z /opt/odm-cli /opt/odm-cli
ENV PATH=/opt/odm-cli/bin:$PATH
```

The build image must be glibc-based (Debian, Ubuntu, Red Hat, Amazon Linux…). Alpine images are not supported. Then use the
job of the [`start-task` GitLab example](./cmd-platform-devops-start-task.md#gitlab-ci-example) unchanged, with that build
image as the job image.

Set the service URLs and any credentials as GitLab CI/CD variables, with **masked** variables for secrets, never in
`.gitlab-ci.yml`.

## Configuration

Configure the CLI with environment variables. Each configuration key maps to a variable in upper case, with `.` replaced by
`_` and dashes removed.

| Variable | Purpose |
| --- | --- |
| `CLI_PLATFORM_SERVICES_REGISTRY_URL` | Registry URL. |
| `CLI_PLATFORM_SERVICES_DEVOPS_URL` | DevOps URL. |
| `CLI_PLATFORM_DEVOPS_POLLINTERVAL`, `CLI_PLATFORM_DEVOPS_ACTIVITYAPPROVALTIMEOUT`, `CLI_PLATFORM_DEVOPS_TASKAPPROVALTIMEOUT` | Polling of the devops commands (defaults `5s`, `30m`, `10m`). |
| `CLI_CLICONFIGURATION_INTERACTIVE` | `false` in the image, so the CLI never waits for input. Set it to `true` only with `docker run -it`. |
| `ODM_CLI_CONFIG_FILE` | Configuration file, default `$HOME/.odmcli/application.yml` (`/home/odmcli/.odmcli/application.yml`). |
| `ODM_CLI_EXTENSIONS_DIR` | Extensions directory, default `/opt/odm-cli/extensions`. |
| `JAVA_TOOL_OPTIONS` | JVM options, for example a proxy: `-Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=3128`. |

**Configuration file**: for list values (`cli.systems`, headers) a file is easier than variables. Mount it at the default
location, or point `ODM_CLI_CONFIG_FILE` to it:

```bash
docker run --rm -v "$PWD":/workspace -w /workspace \
  -v "$HOME/.odmcli/application.yml":/home/odmcli/.odmcli/application.yml:ro \
  ghcr.io/opendatamesh-initiative/odm-cli:X.Y.Z platform registry validate
```

`config --set` and `config --unset` edit that file. Without a mount, the edits are lost when the container exits.

## Extensions and certificates

No extensions are bundled and none are downloaded. To use one, build your own image on top of the CLI image:

```dockerfile
FROM ghcr.io/opendatamesh-initiative/odm-cli:X.Y.Z
COPY my-extension.jar /opt/odm-cli/extensions/
```

or mount a directory of extension jars at `/opt/odm-cli/extensions`.

The Java runtime carries its own CA certificates, in `/opt/odm-cli/runtime/lib/security/cacerts`. To trust a private CA, add
it in your own image:

```dockerfile
FROM ghcr.io/opendatamesh-initiative/odm-cli:X.Y.Z
COPY my-ca.crt /tmp/my-ca.crt
USER root
RUN keytool -importcert -noprompt -alias my-ca -file /tmp/my-ca.crt \
      -keystore /opt/odm-cli/runtime/lib/security/cacerts -storepass changeit
USER 10001:10001
```

or mount a trust store and set `JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStore=/path/to/truststore`.

## Exit codes

The container exits with the CLI's exit code: `0` success, `1` command failure, `2` invalid usage. A pipeline step fails
whenever the code is not `0`.
