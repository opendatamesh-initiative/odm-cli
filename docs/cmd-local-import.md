# odmcli local import

## Usage

`odm-cli local import`

## Description

Import from an external source (e.g., jdbc) to a descriptor target element (e.g., output-port).

The implementation depends on extensions that support `from` and `to` arguments, defining the supported source and target types.

## Options

| Option       | Default                           | Description                                                                                                                    |
|--------------|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| `-f, --file` | PATH/data-product-descriptor.json | Name of the descriptor file                                                                                                    |
| `--from`     |                                   | Source type (e.g., `jdbc`, `template`)                                                                                         |
| `--to`       |                                   | Target element type (e.g., `output-port`)                                                                                      |
| `--target`   |                                   | The target element name. (e.g. If `--to=output-port`, it is the output-port name)                                              |
| `--source`   |                                   | Source arguments. According to the extension documentation. (e.g. for `--from=jdbc` it is a system name defined in the config) |

## Examples

### Create a new output-port from a JDBC table

```bash
odmcli local import \
  -f dpd/data-product-descriptor.json \
  --from jdbc \
  --to output-port \
  --target myPortName \
  --source local-postgres
```

### Create a new output-port from a template

```bash
odmcli local import \
  -f dpd/data-product-descriptor.json \
  --from template \
  --to output-port \
  --source myTemplate \
  --target myPortName 
```
### Supported --to options
The --to option specifies the target element within the Data Product Descriptor (DPD) where the imported data or 
metadata will be integrated. Below is a list of supported types:
* descriptor
* output-port
* input-port
* discovery-port
* observability-port
* control-port

## Extensions

### JDBC Importer Extension
The extension [odm-cli-extensions-importer-jdbc](https://github.com/opendatamesh-initiative/odm-cli-extensions-importer-jdbc)
is a command-line extension for the Open Data Mesh CLI that allows importing database metadata using JDBC. 
It extracts schema, table, and column details and integrates them into a data product.

### Starter Importer Extension
The [odm-cli-extensions-starter](https://github.com/opendatamesh-initiative/odm-cli-extensions-starter) 
repository serves as a boilerplate command-line extension, providing a foundation for developing custom importer.