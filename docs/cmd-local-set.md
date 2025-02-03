# odmcli local set

## Usage

`odmcli local set`

## Description

Set a field value in the descriptor

**⚒️Implementation details:**

1. TODO

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file
`--type`||Element type
`--name`||Element name. 
`--field <PATH=VALUE>` ||The field JSON path and the value to set it


## Examples

### Set some fields' value in the observabiliy-port helthMetrics
```bash
./odmcli local set -f dpd/data-product-descriptor.json \
     --type output-port --name helthMetrics
     --field promises.displayName=Helath Metrics
     --field promises.promises=rest-services
```

### Set data product version
```bash
./odmcli local set -f dpd/data-product-descriptor.json \
     --field info.version=1.2.0
```

*This update also has side effect also on other fields*





