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

COPY ${JAR_FILE} /opt/odm-cli/lib/odm-cli.jar
COPY --chmod=755 docker/odm-cli docker/docker-entrypoint /opt/odm-cli/bin/

# Install the available package updates, so the image ships the security fixes available at build time.
# The user is written to /etc/passwd directly because the base image has no useradd.
RUN set -eu; \
    dnf -y upgrade; \
    dnf clean all; \
    rm -rf /var/cache/dnf; \
    mkdir -p /opt/odm-cli/extensions; \
    ln -s /opt/odm-cli/bin/odm-cli /usr/local/bin/odm-cli; \
    echo "odmcli:x:10001:" >> /etc/group; \
    echo "odmcli:x:10001:10001:ODM CLI:/home/odmcli:/bin/sh" >> /etc/passwd; \
    mkdir -p /home/odmcli /workspace; \
    chown 10001:10001 /home/odmcli /workspace

ENV ODM_CLI_HOME=/opt/odm-cli \
    CLI_CLICONFIGURATION_INTERACTIVE=false

USER 10001:10001
WORKDIR /workspace
ENTRYPOINT ["/opt/odm-cli/bin/docker-entrypoint"]
