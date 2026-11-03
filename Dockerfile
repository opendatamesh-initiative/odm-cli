# Corretto 21 runtime, the Java distribution of the other platform services.
# Its cacerts is a link to the system trust store, which other images do not have: replace it with a copy,
# so the bundle can be copied into another image with its CA certificates.
FROM amazoncorretto:21-al2023-headless AS runtime
RUN set -eu; \
    mkdir -p /opt/odm-cli; \
    cp -a "$(readlink -f "$JAVA_HOME")" /opt/odm-cli/runtime; \
    cacerts=/opt/odm-cli/runtime/lib/security/cacerts; \
    cp --remove-destination "$(readlink -f "$cacerts")" "$cacerts"; \
    chmod 644 "$cacerts"

# Amazon Linux 2023, the OS the Corretto image is built on, which has the runtime's native libraries.
# Copying the runtime onto it keeps a single copy in the image.
FROM amazonlinux:2023

ARG VERSION
ARG REVISION=unknown
ARG JAR_FILE=target/odm-cli-${VERSION}.jar

LABEL org.opencontainers.image.title="odm-cli" \
      org.opencontainers.image.description="Open Data Mesh Platform's CLI" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.source="https://github.com/opendatamesh-initiative/odm-cli" \
      org.opencontainers.image.licenses="Apache-2.0"

# The bundle: everything the CLI needs lives under /opt/odm-cli, so users can copy it into their own build image
COPY --from=runtime /opt/odm-cli/runtime /opt/odm-cli/runtime
COPY ${JAR_FILE} /opt/odm-cli/lib/odm-cli.jar
COPY --chmod=755 docker/odm-cli docker/docker-entrypoint /opt/odm-cli/bin/

RUN set -eu; \
    mkdir -p /opt/odm-cli/extensions; \
    ln -s /opt/odm-cli/bin/odm-cli /usr/local/bin/odm-cli; \
    echo "odmcli:x:10001:" >> /etc/group; \
    echo "odmcli:x:10001:10001:ODM CLI:/home/odmcli:/bin/sh" >> /etc/passwd; \
    mkdir -p /home/odmcli /workspace; \
    chown 10001:10001 /home/odmcli /workspace

ENV JAVA_HOME=/opt/odm-cli/runtime \
    PATH=/opt/odm-cli/runtime/bin:$PATH \
    ODM_CLI_HOME=/opt/odm-cli \
    CLI_CLICONFIGURATION_INTERACTIVE=false

USER 10001:10001
WORKDIR /workspace
ENTRYPOINT ["/opt/odm-cli/bin/docker-entrypoint"]
