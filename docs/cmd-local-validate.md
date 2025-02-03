# odmcli local validate

## Usage

`odmcli local validate`

## Description

Validate a descriptor's syntax. The descriptor is validated against the JSON schema for the DPDS version specified in its `dataProductDescriptor` property.

**⚒️Implementation details:**

1. The validation process MUST be equal to the one applied by the `/validate` endpoint  exposed by the `registry-service` of the ODM Platform
2. The JSON schema MUST be loaded from `⁓/.odmcli/schemas`. If it is not available for the target VERSION, it MUST be retrieved from the DPDS website at https://dpds.opendatamesh.org/specifications/dpds/VERSION/schema.json.

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file

## Examples

### Validate a descriptor file
```bash
./odmcli local validate -f dpd/data-product-descriptor.json
```



