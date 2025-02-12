# odmcli local import

## Usage

`odmcli local import`

## Description

Import from an external source (ex. jdbc) to a descriptor target element  (ex. output-port)

## Options

| Option       | Default                           | Description                                                                                                                        |
|--------------|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `-f, --file` | PATH/data-product-descriptor.json | Name of the descriptor file                                                                                                        |
| `--from`     |                                   | Source type                                                                                                                        |
| `--to`       |                                   | Target type                                                                                                                        |
| `--target`   |                                   | The target sub-type. If `--to=port`, it can be `input-port`, `output-port`, `discovery-port`, `observability-port`, `control-port` |

## Examples

### Create a new output-port from a jdbc-table

```bash
  odmcli local import
  -f dpd/data-product-descriptor.json
  --from jdbc
  --to port
  --target output-port
```

### Create a new output-port from a template

```bash
  odmcli local import
   -f dpd/data-product-descriptor.json 
  --from template 
  --to port
  --target output-port
```



