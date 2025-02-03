# odmcli local list

## Usage

`odmcli local list`

## Description

Lists elements of a descriptor.

**⚒️Implementation details:**

1. TODO

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file

## Examples

### List the names of all input and output ports
```bash
./odmcli local list -f dpd/data-product-descriptor.json \
     --element input-port \
     --element output-port
```

### List the names of all application elements
```bash
./odmcli local list -f dpd/data-product-descriptor.json --element app
```

### List the names of all infrastructural elements
```bash
./odmcli local list -f dpd/data-product-descriptor.json --element infra 
```



