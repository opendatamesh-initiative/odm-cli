# The ODM CLI: the released jar on a Java 21 runtime. It has no entrypoint: "docker run <image> odm-cli <arguments>" runs the
# CLI, and CI jobs (GitLab, GitHub) call odm-cli from their scripts. See docs/run-with-docker.md.
FROM amazoncorretto:21-al2023-headless

ARG VERSION
ARG REVISION=unknown
ARG JAR_FILE=target/odm-cli-${VERSION}.jar

LABEL org.opencontainers.image.title="odm-cli" \
      org.opencontainers.image.description="Open Data Mesh Platform's CLI" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.source="https://github.com/opendatamesh-initiative/odm-cli" \
      org.opencontainers.image.licenses="Apache-2.0"

# The jar, and the launcher that puts odm-cli on PATH
COPY ${JAR_FILE} /opt/odm-cli/lib/odm-cli.jar
COPY --chmod=755 docker/odm-cli /usr/local/bin/odm-cli

# The extensions directory, a non-root user and its working directory, where CI runners and "docker run -v" mount the
# files. The user is written to /etc/passwd directly because the base image has no useradd.
RUN set -eu; \
    mkdir -p /opt/odm-cli/extensions; \
    echo "odmcli:x:10001:" >> /etc/group; \
    echo "odmcli:x:10001:10001:ODM CLI:/home/odmcli:/bin/sh" >> /etc/passwd; \
    mkdir -p /home/odmcli /workspace; \
    chown 10001:10001 /home/odmcli /workspace

# No prompts: a missing value fails instead of waiting for input
ENV CLI_CLICONFIGURATION_INTERACTIVE=false

USER 10001:10001
WORKDIR /workspace
CMD ["odm-cli", "--help"]
