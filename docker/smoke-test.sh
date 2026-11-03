#!/bin/sh
# Smoke tests for a built ODM CLI image, run before it is pushed.
# Usage: docker/smoke-test.sh <image> <expected version>
# Each check maps to one scenario of spdd/prompt/BDMD-5443-202611021728-[Feat]-docker-cli-image.md.
# No live external systems are used: the ".invalid" hosts never resolve.
set -u

if [ $# -ne 2 ]; then
  echo "Usage: $0 <image> <expected version>" >&2
  exit 2
fi
IMAGE=$1
EXPECTED_VERSION=$2

REPO_DIR=$(dirname "$(dirname "$(readlink -f "$0")")")
DESCRIPTOR="$REPO_DIR/src/test/resources/org/opendatamesh/cli/usecases/descriptorvalidator/test_descriptorvalidator_data_product_descriptor.json"
WORK_DIR=$(mktemp -d)
BUNDLE_IMAGE="odm-cli-smoke-bundle:$$"
FAILURES=0

cleanup() {
  rm -rf "$WORK_DIR"
  docker image rm -f "$BUNDLE_IMAGE" >/dev/null 2>&1
}
trap cleanup EXIT

# Workspace readable by the image user (UID 10001), whatever the owner of the checkout
mkdir -p "$WORK_DIR/workspace"
cp "$DESCRIPTOR" "$WORK_DIR/workspace/descriptor.json"
chmod 755 "$WORK_DIR" "$WORK_DIR/workspace"
chmod 644 "$WORK_DIR/workspace/descriptor.json"

# Runs a container and stores its combined output in OUTPUT and its exit code in STATUS
run() {
  OUTPUT=$(docker run --rm "$@" 2>&1)
  STATUS=$?
}

pass() {
  echo "PASS $1"
}

fail() {
  echo "FAIL $1: $2"
  echo "$OUTPUT" | sed 's/^/    /'
  FAILURES=$((FAILURES + 1))
}

last_line() {
  echo "$OUTPUT" | tail -n 1
}

# Scenario: The image reports the release version
#   Given an image built from the CLI jar of version X
#   When the user runs the image with "--version"
#   Then the output ends with "X" and the exit code is 0
checkVersion() {
  run "$IMAGE" --version
  if [ "$STATUS" -eq 0 ] && [ "$(last_line)" = "$EXPECTED_VERSION" ]; then
    pass checkVersion
  else
    fail checkVersion "expected version $EXPECTED_VERSION and exit code 0, got exit code $STATUS"
  fi
}

# Scenario: Help is available
#   When the user runs the image with "--help"
#   Then the top-level commands are listed and the exit code is 0
checkHelp() {
  run "$IMAGE" --help
  if [ "$STATUS" -eq 0 ] && echo "$OUTPUT" | grep -q "platform"; then
    pass checkHelp
  else
    fail checkHelp "expected the command list and exit code 0, got exit code $STATUS"
  fi
}

# Scenario: Invalid usage keeps the CLI exit code
#   When the user runs the image with an unknown option
#   Then the exit code is 2
checkInvalidUsage() {
  run "$IMAGE" --no-such-option
  if [ "$STATUS" -eq 2 ]; then
    pass checkInvalidUsage
  else
    fail checkInvalidUsage "expected exit code 2, got $STATUS"
  fi
}

# Scenario: A shell command runs the CLI without an entrypoint override
#   Given GitLab starts the job container with a shell command
#   When the job script runs "odm-cli --version"
#   Then the CLI prints the version and the exit code is 0
checkShellCommand() {
  run "$IMAGE" sh -c 'odm-cli --version'
  if [ "$STATUS" -eq 0 ] && [ "$(last_line)" = "$EXPECTED_VERSION" ]; then
    pass checkShellCommand
  else
    fail checkShellCommand "expected version $EXPECTED_VERSION and exit code 0, got exit code $STATUS"
  fi
}

# Scenario: The bundle runs in another glibc image
#   Given a build image that copies /opt/odm-cli from the CLI image
#   When the job runs /opt/odm-cli/bin/odm-cli --version
#   Then the CLI prints the version and the exit code is 0
#   And the bundle carries its own CA certificates
checkCopiedBundle() {
  OUTPUT=$(printf 'FROM %s AS cli\nFROM debian:stable-slim\nCOPY --from=cli /opt/odm-cli /opt/odm-cli\n' "$IMAGE" \
    | docker build -q -t "$BUNDLE_IMAGE" - 2>&1)
  if [ $? -ne 0 ]; then
    fail checkCopiedBundle "could not build an image that copies the bundle"
    return
  fi
  run "$BUNDLE_IMAGE" /opt/odm-cli/bin/odm-cli --version
  if [ "$STATUS" -ne 0 ] || [ "$(last_line)" != "$EXPECTED_VERSION" ]; then
    fail checkCopiedBundle "expected version $EXPECTED_VERSION and exit code 0, got exit code $STATUS"
    return
  fi
  CACERTS=/opt/odm-cli/runtime/lib/security/cacerts
  run "$BUNDLE_IMAGE" sh -c "test -f $CACERTS && test ! -L $CACERTS && test -s $CACERTS"
  if [ "$STATUS" -eq 0 ]; then
    pass checkCopiedBundle
  else
    fail checkCopiedBundle "$CACERTS is missing, empty or a link"
  fi
}

# Scenario: An environment variable sets the registry URL
#   Given the variable CLI_PLATFORM_SERVICES_REGISTRY_URL points to an unreachable host
#   When the user validates a descriptor in the mounted workspace
#   Then the CLI fails with exit code 1 and names that host
checkEnvironmentConfiguration() {
  run -v "$WORK_DIR/workspace:/workspace:ro" \
    -e CLI_PLATFORM_SERVICES_REGISTRY_URL=http://odm-smoke-env.invalid:9999 \
    "$IMAGE" platform registry validate -f descriptor.json
  if [ "$STATUS" -eq 1 ] && echo "$OUTPUT" | grep -q "odm-smoke-env.invalid"; then
    pass checkEnvironmentConfiguration
  else
    fail checkEnvironmentConfiguration "expected exit code 1 naming odm-smoke-env.invalid, got exit code $STATUS"
  fi
}

# Scenario: A mounted configuration file sets the registry URL
#   Given a configuration file mounted at the default location points the registry to an unreachable host
#   When the user validates a descriptor in the mounted workspace
#   Then the CLI fails with exit code 1 and names that host
checkMountedConfigurationFile() {
  CONFIG_FILE="$WORK_DIR/application.yml"
  printf 'cli:\n  platform:\n    services:\n      registry:\n        url: http://odm-smoke-file.invalid:9999\n' > "$CONFIG_FILE"
  chmod 644 "$CONFIG_FILE"
  run -v "$WORK_DIR/workspace:/workspace:ro" \
    -v "$CONFIG_FILE:/home/odmcli/.odmcli/application.yml:ro" \
    "$IMAGE" platform registry validate -f descriptor.json
  if [ "$STATUS" -eq 1 ] && echo "$OUTPUT" | grep -q "odm-smoke-file.invalid"; then
    pass checkMountedConfigurationFile
  else
    fail checkMountedConfigurationFile "expected exit code 1 naming odm-smoke-file.invalid, got exit code $STATUS"
  fi
}

# Scenario: The image never waits for input
#   When the image starts without overrides
#   Then interactive mode is off by default
checkNonInteractiveDefault() {
  run "$IMAGE" sh -c 'echo "$CLI_CLICONFIGURATION_INTERACTIVE"'
  if [ "$STATUS" -eq 0 ] && [ "$(last_line)" = "false" ]; then
    pass checkNonInteractiveDefault
  else
    fail checkNonInteractiveDefault "expected CLI_CLICONFIGURATION_INTERACTIVE=false"
  fi
}

# Scenario: The CLI runs as a non-root user
#   When the user runs "id -u" in the image
#   Then the output is 10001
checkNonRootUser() {
  run "$IMAGE" id -u
  if [ "$STATUS" -eq 0 ] && [ "$(last_line)" = "10001" ]; then
    pass checkNonRootUser
  else
    fail checkNonRootUser "expected UID 10001"
  fi
}

checkVersion
checkHelp
checkInvalidUsage
checkShellCommand
checkCopiedBundle
checkEnvironmentConfiguration
checkMountedConfigurationFile
checkNonInteractiveDefault
checkNonRootUser

if [ "$FAILURES" -ne 0 ]; then
  echo "$FAILURES smoke test(s) failed for $IMAGE"
  exit 1
fi
echo "All smoke tests passed for $IMAGE"
