# odmcli local init

## Usage

`odm-cli local init`

## Description

This command initializes a new data product descriptor template with the given domain and name. The template can be customized using optional parameters. The initialized descriptor is stored in a JSON or YAML file.

## Options

| Option                | Default                                          | Description                                                                                                                              |
|-----------------------|--------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `--domain`            |                                                  | Mandatory: The domain of the data product (e.g., `finance`, `marketing`).                                                                    |
| `--name`              |                                                  | Mandatory: The name of the data product (e.g., `product-sales`, `customer-engagement`).                                                      |
| `--displayName`       | Generated from `--name`                            | Optional: A human-readable name for the data product. Default is generated from `--name`.                                                  |
| `--descriptorVersion` | `1.0.0`                                            | Optional: The version of the data product. Default is `1.0.0`.                                                                             |
| `--fullyQualifiedName` | `urn:dpds:{domain}:dataproducts:{name}:{major version}` | Optional: A unique identifier for the data product. Default is generated from `--domain`, `--name` and the major version of `--descriptorVersion` (see [Default fully qualified name](#default-fully-qualified-name)). |
| `--description`       | null                                             | Optional: A brief description of the data product. Default is null.                                                                      |
| `--ownerId`           | Collected from the CLI configuration (if provided) | Optional: The ID of the owner of the data product. If not provided, this will be loaded from the CLI configurations.                     |
| `--ownerName`          | Collected from the CLI configuration (if provided) | Optional: The name of the owner of the data product. If not provided, this will be loaded from the CLI configurations.                   |
| `--outputFile`         | `./data-product-descriptor.json`                   | Optional: Specifies the path where the template will be stored, either a json or a yaml file. Default is `./data-product-descriptor.json`. |
| `--force`              | false                                            | Optional: Forces the overwriting of the output file if it already exists.                                                                |

## Default fully qualified name

When `--fullyQualifiedName` is not given, it is generated following the DPDS convention:

```
urn:dpds:{domain}:dataproducts:{name}:{major version}
```

where `{major version}` is the major version of `--descriptorVersion` (e.g. `urn:dpds:finance:dataproducts:product-sales:1`
for version `1.0.0`). If the version doesn't start with a number, the version segment is omitted:
`urn:dpds:{domain}:dataproducts:{name}`.

Ports added to the descriptor with [`odmcli local import`](cmd-local-import.md) build their fully qualified names on top
of this one (e.g. `urn:dpds:finance:dataproducts:product-sales:1:outputports:{port name}:{port major version}`).

> **Changed in 2.0.0**: previous versions generated `{domain}:{name}:null`. Descriptors created with older versions keep
> their fully qualified name; set `--fullyQualifiedName` explicitly to use a custom format.

## Examples

### Initialize a Data Product Descriptor

```bash
odmcli local init \
  --domain finance \
  --name product-sales \
  --descriptorVersion 1.0.0 \
  --description "This template describes the product sales data." \
  --outputFile ./product-sales-template.json
```


### Initialize a Data Product Descriptor

```bash
odmcli local init \
  --domain marketing \
  --name customer-engagement \
  --displayName "Customer Engagement Data" \
  --descriptorVersion 1.2.3 \
  --fullyQualifiedName "urn:dpds:marketing:dataproducts:customer-engagement:1" \
  --ownerId "owner123" \
  --ownerName "John Doe" \
  --outputFile ./customer-engagement-template.json
```


### Force Overwriting of the Output File

```bash
odmcli local init \
  --domain finance \
  --name product-sales \
  --outputFile ./product-sales-template.json \
  --force
```

